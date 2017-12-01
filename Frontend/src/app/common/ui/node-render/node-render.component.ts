import {
  Component, OnInit, OnDestroy, Input, EventEmitter, Output, ViewChild, ElementRef,
  HostListener, ChangeDetectorRef, ApplicationRef
} from '@angular/core';
import {RestConnectorService} from "../../rest/services/rest-connector.service";
import {RestConstants} from "../../rest/rest-constants";
import {NodeList, Node, NodeWrapper, LoginResult, ConnectorList} from "../../rest/data-object";
import {Toast} from "../toast";
import {RestNodeService} from "../../rest/services/rest-node.service";
import {ActivatedRoute, Params, Router} from "@angular/router";
import {TranslateService} from "ng2-translate";
import {Translation} from "../../translation";
import {TemporaryStorageService} from "../../services/temporary-storage.service";
import {OptionItem} from "../actionbar/actionbar.component";
import {UIAnimation} from "../ui-animation";
import {FrameEventsService} from "../../services/frame-events.service";
import {RouterComponent} from "../../../router/router.component";
import {UIHelper} from "../ui-helper";
import {ConfigurationService} from "../../services/configuration.service";
import {Title} from "@angular/platform-browser";
import {SessionStorageService} from "../../services/session-storage.service";
import {RestConnectorsService} from "../../rest/services/rest-connectors.service";
import {WorkspaceMainComponent} from "../../../modules/workspace/workspace.component";
import {trigger} from "@angular/animations";
import {NodeHelper} from "../node-helper";
import {RestToolService} from "../../rest/services/rest-tool.service";
import {UIConstants} from "../ui-constants";

declare var jQuery:any;
declare var window: any;

@Component({
  selector: 'node-render',
  templateUrl: 'node-render.component.html',
  styleUrls: ['node-render.component.scss'],
  animations: [
    trigger('fadeFast', UIAnimation.fade(UIAnimation.ANIMATION_TIME_FAST))
  ]
})


export class NodeRenderComponent {


  public isLoading=true;
  /**
   * Show a bar at the top with the node name or not
   * @type {boolean}
   */
  @Input() showTopBar=true;
  /**
   * Node version, -1 indicates the latest
   * @type {string}
   */
  @Input() version=RestConstants.NODE_VERSION_CURRENT;
  /**
   *   display metadata
   */
  @Input() metadata=true;
  private isRoute = false;
  private options: OptionItem[]=[];
  private list: Node[];
  private isSafe=false;
  private isOpenable: boolean;
  private closeOnBack: boolean;
  public nodeMetadata: Node;
  private editor: string;
  private fromLogin = false;

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event:any) {
    if(this.isSafe){
      this.connector.logoutSync();
    }
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if(this.nodeMetadata!=null) {
      return;
    }
    if(event.code=="Escape"){
      event.preventDefault();
      event.stopPropagation();
      this.close();
      return;
     }
    if (event.code == "ArrowLeft" && this.canSwitchBack()) {
      this.node = this.list[this.getPosition() - 1];
      event.preventDefault();
      event.stopPropagation();
      return;
    }
    if (event.code == "ArrowRight" && this.canSwitchForward()) {
      this.node = this.list[this.getPosition() + 1];
      event.preventDefault();
      event.stopPropagation();
      return;
    }

  }
    private _node : Node;
    @Input() set node(node: Node|string){
      let id=(node as Node).ref ? (node as Node).ref.id : (node as string);
      if((node as Node).ref && (node as Node).name) {
        this._node = (node as Node);
      }else{
        this.nodeApi.getNodeRenderSnippet(id).subscribe((data:NodeWrapper)=>{
          this._node=data.node;
          this.loadNode();
        },(error:any)=>{
          if(error.status==401)
            return;
          this.toast.error(error);
        });
        return;
      }
      this.loadNode();
    }
    @Output() onClose=new EventEmitter();
    private close(){
      if(this.isRoute) {
        if(this.closeOnBack){
          window.close();
        }
        else {
          if(this.fromLogin){
            this.router.navigate([UIConstants.ROUTER_PREFIX+"workspace"]);
          }
          else {
            NodeRenderComponent.close();
          }
        }
      }
      else
        this.onClose.emit();
    }


    private showDetails() {
      window.showDetails();
    }
    public getPosition(){
      if(!this._node || !this.list)
        return -1;
      let i=0;
      for(let node of this.list){
        if(node.ref.id==this._node.ref.id)
          return i;
        i++;
      }
      return -1;
    }
    constructor(
      private translate : TranslateService,
      private connector : RestConnectorService,
      private connectors : RestConnectorsService,
      private nodeApi : RestNodeService,
      private toolService: RestToolService,
      private frame : FrameEventsService,
      private toast : Toast,
      private title : Title,
      private config : ConfigurationService,
      private storage : SessionStorageService,
      private route : ActivatedRoute,
      private router : Router,
      private temporaryStorageService: TemporaryStorageService) {
      Translation.initialize(translate,config,storage,route).subscribe(()=>{
        this.connector.setRoute(this.route);
        this.route.queryParams.subscribe((params:Params)=>{
          this.closeOnBack=params['closeOnBack']=='true';
          this.editor=params['editor'];
          this.fromLogin=params['fromLogin']=='true';
        });
        this.route.params.subscribe((params: Params) => {
          if(params['node']) {
            this.isRoute=true;
            this.list = this.temporaryStorageService.get(TemporaryStorageService.NODE_RENDER_PARAMETER_LIST);
            this.connector.isLoggedIn().subscribe((data:LoginResult)=>{
              this.isSafe=data.currentScope==RestConstants.SAFE_SCOPE;
              if(params['version'])
                this.version=params['version'];
              setTimeout(()=>this.node = params['node'],10);
            });
          }
        });
      });
      this.frame.broadcastEvent(FrameEventsService.EVENT_VIEW_OPENED,'node-render');
    }

  public static close() {
    window.history.back();
  }
  public switchPosition(pos:number){
    //this.router.navigate([UIConstants.ROUTER_PREFIX+"render",this.list[pos].ref.id]);
    this.isLoading=true;
    this.node=this.list[pos];
    //this.options=[];
  }
  public canSwitchBack(){
    return this.list && this.getPosition()>0 && !this.list[this.getPosition()-1].isDirectory;
  }
  public canSwitchForward(){
    return this.list && this.getPosition()<this.list.length-1 && !this.list[this.getPosition()+1].isDirectory;
  }
  public closeMetadata(){
    this.nodeMetadata=null;
  }
  public refresh(){
    this.loadNode();

  }
  private loadNode() {
      if(!this._node)
        return;

    let input=this.temporaryStorageService.get(TemporaryStorageService.NODE_RENDER_PARAMETER_OPTIONS);
    if(!input) input=[];
    //this.options = JSON.parse(JSON.stringify(this.temporaryStorageService.get(NodeRenderComponent.OPTIONS)));
    let opt=[];
    for(let o of input){
      opt.push(o);
    }
    this.options=opt;
    console.log(this._node);
    let download=new OptionItem('DOWNLOAD','cloud_download',()=>this.downloadCurrentNode());
    download.isEnabled=this._node.downloadUrl!=null;
    if(this.isCollectionRef()){
      console.log("is ref");
      this.nodeApi.getNodeMetadata(this._node.properties[RestConstants.CCM_PROP_IO_ORIGINAL]).subscribe((node:NodeWrapper)=>{
        this.addDownloadButton(download);
      },(error:any)=>{
        if(error.status==RestConstants.HTTP_NOT_FOUND) {
          console.log("original missing");
          download.isEnabled = false;
        }
        this.addDownloadButton(download);
      });
      return;
    }
    this.addDownloadButton(download);
  }
  private loadRenderData(){
    let parent=this;
    let url=this.connector.endpointUrl + this.nodeApi.getNodeRenderSnippetUrl(this._node.ref.id,this.version ? this.version : "-1");

    let parameters={
      showDownloadButton:false,
      showDownloadAdvice:!this.isOpenable
    };
    jQuery.ajax({
      url: url,
      method:this.connector.getApiVersion()>=RestConstants.API_VERSION_4_0 ? 'POST' : 'GET',
      contentType: "application/json",
      data:JSON.stringify(parameters),
      dataType: 'json',
      crossDomain : true,
      xhrFields: {
        withCredentials: true
      },
      success:function(data: any) {
        if (!data.detailsSnippet) {
          console.error(data);
          parent.toast.error(null,"error while reaching render service. Please check your server configuration or report this error.");
        }
        else {
          jQuery('#nodeRenderContent').html(data.detailsSnippet);
          parent.postprocessHtml();
        }
        parent.isLoading = false;
      },
      error:function(data : any) {
        //jQuery('#nodeRenderContent').html('Error fetching ' + url);
        console.log(data);
        parent.toast.error(JSON.parse(data.responseText).message);
        parent.isLoading = false;
      }
    });
  }

  private postprocessHtml() {
    if(!this.config.instant("rendering.showPreview",true)){
      jQuery('.edusharing_rendering_content_wrapper').hide();
    }
  }

  private downloadCurrentNode() {
    NodeHelper.downloadNode(this._node,this.version);
  }

  private openConnector(list:ConnectorList,newWindow=true) {
    if(RestToolService.isLtiObject(this._node)){
      this.toolService.openLtiObject(this._node);
    }
    else {
      this.connectors.openConnector(list, this._node,null,null,null,newWindow);
    }
  }

  private checkConnector() {
    this.connector.isLoggedIn().subscribe((login:LoginResult)=>{
      if(!this.isCollectionRef()){
        let openFolder = new OptionItem('SHOW_IN_FOLDER', 'folder', () => this.goToWorkspace(login,this._node));
        openFolder.isEnabled=false;
        this.nodeApi.getNodeParents(this._node.parent.id,false,[],this._node.parent.repo).subscribe((data:NodeList)=>{
          openFolder.isEnabled = true;
        });
        if(this._node.type!=RestConstants.CCM_TYPE_REMOTEOBJECT)
          this.options.push(openFolder);

        let edit=new OptionItem('WORKSPACE.OPTION.EDIT','info_outline',()=>this.nodeMetadata=this._node);
        edit.isEnabled=this._node.access.indexOf(RestConstants.ACCESS_WRITE)!=-1 && this._node.type!=RestConstants.CCM_TYPE_REMOTEOBJECT;
        if(this.version==RestConstants.NODE_VERSION_CURRENT)
          this.options.push(edit);
        this.isOpenable=false;
      }
      else{
        let openFolder = new OptionItem('SHOW_IN_FOLDER', 'folder', () => this.goToWorkspace(login,this._node));
        openFolder.isEnabled = false;
        this.nodeApi.getNodeMetadata(this._node.properties[RestConstants.CCM_PROP_IO_ORIGINAL]).subscribe((node:NodeWrapper)=>{

          this.nodeApi.getNodeParents( node.node.parent.id,false,[], node.node.parent.repo).subscribe((data:NodeList)=>{
            openFolder.isEnabled = true;
            //.isEnabled = data.node.access.indexOf(RestConstants.ACCESS_WRITE) != -1;
          });
        },(error:any)=>{
        });
        this.options.push(openFolder);
      }

      this.connectors.list().subscribe((data:ConnectorList)=>{
        if(this.version==RestConstants.NODE_VERSION_CURRENT && RestConnectorsService.connectorSupportsEdit(data,this._node) || RestToolService.isLtiObject(this._node)){
          let view=new OptionItem("WORKSPACE.OPTION.VIEW", "launch",()=>this.openConnector(data,true));
          //view.isEnabled = this._node.access.indexOf(RestConstants.ACCESS_WRITE)!=-1;
          this.options.splice(0,0,view);
          this.options=this.options.slice();
          this.isOpenable=true;
          if(this.editor && RestConnectorsService.connectorSupportsEdit(data,this._node).id==this.editor){
            this.openConnector(data,false);
          }
        }
        this.loadRenderData();
      },(error:any)=>{
        this.loadRenderData();
      });


    });
  }

  private goToWorkspace(login:LoginResult,node:Node) {
      NodeHelper.goToWorkspace(this.nodeApi,this.router,login,node);
  }

  private isCollectionRef() {
    return this._node.aspects.indexOf(RestConstants.CCM_ASPECT_IO_REFERENCE)!=-1;
  }

  private addDownloadButton(download: OptionItem) {
    this.options.splice(0,0,download);
    this.checkConnector();

    UIHelper.setTitleNoTranslation(this._node.name,this.title,this.config);
    this.isLoading=true;
  }
}
