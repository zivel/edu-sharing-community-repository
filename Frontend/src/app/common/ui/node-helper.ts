import {RestConstants} from "../rest/rest-constants";
import {TranslateService} from "ng2-translate";
import {
  Node, Permission, Collection, User, LoginResult, AuthorityProfile, ParentList,
  Repository, WorkflowDefinition
} from "../rest/data-object";
import {FormatSizePipe} from "./file-size.pipe";
import {RestConnectorService} from "../rest/services/rest-connector.service";
import {Observable, Observer} from "rxjs";
import {Response, ResponseContentType, Http} from "@angular/http";
import {ConfigurationService} from "../services/configuration.service";
import {RestHelper} from "../rest/rest-helper";
import {Toast} from "./toast";
import {Router} from "@angular/router";
import {OptionItem} from "./actionbar/actionbar.component";
import {DateHelper} from "./DateHelper";
import {RestNodeService} from "../rest/services/rest-node.service";
import {UIConstants} from "./ui-constants";
import {Translation} from "../translation";
import {TemporaryStorageService} from "../services/temporary-storage.service";
import {ApplyToLmsComponent} from "./apply-to-lms/apply-to-lms.component";
import {ListItem} from "./list-item";
import {Helper} from "../helper";

export class NodeHelper{
  /**
   * Gets and formats an attribute from the node
   * @param node
   * @param item
   * @returns {any}
   */
  public static getNodeAttribute(translation : TranslateService,config:ConfigurationService,node : Node,item : string|ListItem) : string
  {
    let name:string;
    if(typeof item !== "string"){
      name=item.name;
    }
    else{
      name=item;
    }
    if(name==RestConstants.CM_NAME)
      return node["name"];
    if(name==RestConstants.CM_PROP_TITLE){
      return RestHelper.getTitle(node);
    }
    if(name==RestConstants.SIZE) {
      return node.size ? (new FormatSizePipe().transform(node.size,null) as string) : translation.instant('NO_SIZE');
    }
    if(name==RestConstants.MEDIATYPE){
      return translation.instant("MEDIATYPE."+node.mediatype);
    }
    if(name==RestConstants.CM_CREATOR){
      return RestHelper.getPersonWithConfigDisplayName(node.createdBy,config);
    }
    if(name==RestConstants.CCM_PROP_WF_STATUS && !node.isDirectory){
      let workflow=NodeHelper.getWorkflowStatus(config,node);
      return '<div class="workflowStatus" style="background-color: '+workflow.color+'">'+translation.instant('WORKFLOW.'+workflow.id)+'</div>'
    }
    if(name==RestConstants.DIMENSIONS){
      let width=node.properties[RestConstants.CCM_PROP_WIDTH];
      let height=node.properties[RestConstants.CCM_PROP_HEIGHT];
      let megapixel=Math.round((width*height)/1000000.);
      if(width && height) {
        if(megapixel>1){
          return megapixel+" Megapixel";
        }
        return Math.round(width) + "x" + Math.round(height);
      }
    }
    let value : string;
    if(node.properties[name])
      value=node.properties[name].join(", ");
    if((node as any)[name])
      value=(node as any)[name];

    if(value && RestConstants.DATE_FIELDS.indexOf(name)!=-1){
      if(typeof item !== "string" && item.format){
        value=DateHelper.formatDateByPattern(value,item.format);
      }
      else {
        value = DateHelper.formatDate(translation, value);
      }
      if(node.properties[name])
        node.properties[name][0]=value;
    }
    if(node.properties[name+RestConstants.DISPLAYNAME_SUFFIX]){
      value=node.properties[name+RestConstants.DISPLAYNAME_SUFFIX].join(", ");
    }
    if(value)
      return value;
    return "-";
    //return "MISSING "+item;

  }

  /**
   * returns true if all nodes have the requested right
   * @param nodes
   * @param right
   * @returns {boolean}
   */
  public static getNodesRight(nodes : Node[],right : string){
    if(nodes==null)
      return true;
    for(let node of nodes){
      if(!node.access)
        return false;
      if(node.access.indexOf(right)==-1)
        return false;
    }
    return true;
  }
  public static handleNodeError(toast:Toast,name: string, error: any) : number{
    console.log(error);

    if(error.status==RestConstants.DUPLICATE_NODE_RESPONSE){
      toast.error(null,"WORKSPACE.TOAST.DUPLICATE_NAME",{name:name});
      return error.status;
    }
    else if(error._body){
      try{
        let json=JSON.parse(error._body);
        if(json.message.startsWith("org.alfresco.service.cmr.repository.CyclicChildRelationshipException")){
          toast.error(null,"WORKSPACE.TOAST.CYCLIC_NODE",{name:name});
          return error.status;
        }
      }
      catch(e){}
    }
    toast.error(error);
    return error.status;
  }

  /**
   * Navigate to the workspace
   * @param nodeService instance of NodeService
   * @param router instance of Router
   * @param login a result of the isValidLogin method
   * @param node The node to open and show
   */
  public static goToWorkspace(nodeService:RestNodeService,router:Router,login:LoginResult,node:Node) {
    nodeService.getNodeParents(node.ref.id).subscribe((data:ParentList)=>{
      router.navigate([UIConstants.ROUTER_PREFIX+"workspace/"+(login.currentScope ? login.currentScope : "files")],
        {queryParams:{id:node.parent.id,file:node.ref.id,root:data.scope}});
    });
  }
  /**
   * Navigate to the workspace
   * @param nodeService instance of NodeService
   * @param router instance of Router
   * @param login a result of the isValidLogin method
   * @param folder The folder id to open
   */
  public static goToWorkspaceFolder(nodeService:RestNodeService,router:Router,login:LoginResult,folder:string) {
    router.navigate([UIConstants.ROUTER_PREFIX+"workspace/"+(login.currentScope ? login.currentScope : "files")],
      {queryParams:{id:folder}});
  }

  /**
   * Get a formatted attribute from a collection
   * @param translate
   * @param collection
   * @param item
   * @returns {any}
   */
  public static getCollectionAttribute(translate : TranslateService,collection : any,item : string) : string
  {
    if(item=='info'){
      let childs=collection.childReferencesCount;
      let coll=collection.childCollectionsCount;

      return '<i class="material-icons">layers</i> '+coll+' <i class="material-icons">insert_drive_file</i> '+childs;
      /*
      let result="";
      if(coll>0){
        result=coll+" "+translate.instant("COLLECTION.INFO_REFERENCES"+(coll>1 ? "_MULTI" : ""));
      }
      if(childs>0){
        if(coll>0)
          result+=", ";
        result+=childs+" "+translate.instant("COLLECTION.INFO_CHILDS"+(childs>1 ? "_MULTI" : ""));
      }
      if(coll+childs==0){
        return translate.instant("COLLECTION.INFO_NO_CONTENT");
      }
      return result;
          */
    }
    if(item=='scope'){
      let scope=collection.scope;
      let icon="help";
      let scopeName="help";
      if(scope==RestConstants.COLLECTIONSCOPE_MY){
        icon="lock";
        scopeName="MY";
      }
      if(scope==RestConstants.COLLECTIONSCOPE_ORGA || scope==RestConstants.COLLECTIONSCOPE_CUSTOM){
        icon="group";
        scopeName="SHARED";
      }
      if(scope==RestConstants.COLLECTIONSCOPE_ALL || scope==RestConstants.COLLECTIONSCOPE_CUSTOM_PUBLIC){
        icon="language";
        scopeName="PUBLIC";
      }
      return '<i class="material-icons collectionScope">'+icon+'</i> '+translate.instant('COLLECTION.SCOPE.'+scopeName);
    }
    return collection[item];
  }

  /**
   * Download (a single) node
   * @param node
   */
  public static downloadNode(node:Node,version=RestConstants.NODE_VERSION_CURRENT) {
    window.open(node.downloadUrl+(version && version!=RestConstants.NODE_VERSION_CURRENT ? "&version="+version : ""));
  }


  /**
   * fetches the preview of the node and appends it at preview.data
   * @param node
   */
  public static appendImageData(rest:RestConnectorService,node: Node,quality=60) : Observable<Node>{
  return new Observable<Node>((observer : Observer<Node>)=>{
    let options=rest.getRequestOptions();
    options.responseType=ResponseContentType.Blob;

    rest.get(node.preview.url+"&quality="+quality,options,false).subscribe((data:Response)=>{
    //rest.get("http://localhost:8081/edu-sharing/rest/authentication/v1/validateSession",options,false).subscribe((data:Response)=>{
      node.preview.data=data.blob();
      observer.next(node);
      observer.complete();
      /*
      var reader = new FileReader();
      console.log(data.blob());
      reader.readAsDataURL(data.blob());
      reader.onloadend = function() {
        node.preview.data=reader.result;
        console.log(node.preview.data);
        observer.next(node);
        observer.complete();
      }
      */
    });
  });
  }

  /**
   * Return the license icon of a node
   * @param node
   * @returns {string}
   */
  public static getLicenseIcon(node: Node) {
    return node.licenseURL;
  }

  /**
   * Get a license icon by using the property value string
   * @param string
   * @param rest
   * @returns {string}
   */
  public static getLicenseIconByString(string: String,rest:RestConnectorService) {
    let icon=string.replace(/_/g,"-").toLowerCase();
    if(icon=='')
      icon='none';
    return rest.getAbsoluteEndpointUrl()+"../ccimages/licenses/"+icon+".svg";
  }

  /**
   * Return a translated name of a license name for a node
   * @param node
   * @param translate
   * @returns {string|any|string|any|string|any|string|any|string|any|string}
   */
  public static getLicenseName(node: Node,translate:TranslateService) {
    let prop=node.properties[RestConstants.CCM_PROP_LICENSE];
    if(prop)
      prop=prop[0];
    else
      prop='';
    return NodeHelper.getLicenseNameByString(prop,translate);

  }

  /**
   * Return a translated name for a license string
   * @param string
   * @param translate
   * @returns {any}
   */
  public static getLicenseNameByString(string:String,translate:TranslateService) {
    let name=string.replace(/_/g,"-");
    if(name=='CUSTOM')
      return translate.instant("LICENSE.CUSTOM");
    if(name==''){
      return translate.instant("LICENSE.NONE");
    }
    if(name=='MULTI')
      return translate.instant("LICENSE.MULTI");
    if(name=='SCHULFUNK')
      return translate.instant("LICENSE.SCHULFUNK");
    if(name.startsWith("COPYRIGHT")){
      return translate.instant("LICENSE."+string);
    }
    return name;
  }

  /**
   * return the License URL (e.g. for CC_BY licenses) for a license string and version
   * @param licenseProperty
   * @param licenseVersion
   */
  public static getLicenseUrlByString(licenseProperty: string,licenseVersion:string) {
    return (RestConstants.LICENSE_URLS as any)[licenseProperty].replace("#version",licenseVersion);
  }

  /**
   * Get a user name for displaying
   * @param user
   * @returns {string}
   */
  public static getUserDisplayName(user:AuthorityProfile|User){
    return (user.profile.firstName+" "+user.profile.lastName).trim();
  }

  /**
   * Get an attribute (property) from a node
   * @param translate
   * @param config
   * @param data The node or other object to use
   * @param item The ListItem info for which the value should be resolved
   * @returns {any}
   */
  public static getAttribute(translate:TranslateService,config:ConfigurationService,data : any,item : ListItem) : string{
    if(item.type=='NODE') {
      if (item.name == RestConstants.CM_MODIFIED_DATE)
        return '<span property="dateModified" title="' + translate.instant('ACCESSIBILITY.LASTMODIFIED') + '">' + NodeHelper.getNodeAttribute(translate,config, data, item) + '</span>';

      if (item.name == RestConstants.CCM_PROP_LICENSE) {
        if (data.licenseURL) {
          return NodeHelper.getLicenseHtml(translate,data);
        }
        return '';
      }
      if (item.name == RestConstants.CCM_PROP_REPLICATIONSOURCE) {
        if (typeof data.properties[RestConstants.CCM_PROP_REPLICATIONSOURCE] !== 'undefined' && data.properties[RestConstants.CCM_PROP_REPLICATIONSOURCE] != '') {
          let rawSrc = data.properties[RestConstants.CCM_PROP_REPLICATIONSOURCE].toString();
          let src = rawSrc.substring(rawSrc.lastIndexOf(":") + 1).toLowerCase();
          return '<img alt="'+src+'" src="'+NodeHelper.getSourceIconPath(src)+'">';
        }
        return '<img alt="" src="'+NodeHelper.getSourceIconPath('home')+'">';
      }
      return NodeHelper.getNodeAttribute(translate,config, data, item);
    }
    if(item.type=='COLLECTION'){
      return NodeHelper.getCollectionAttribute(translate,data.collection,item.name);
    }
    if(item.type=='GROUP' || item.type=='ORG'){
      if(item.name=="displayName")
        return data.profile.displayName;
    }
    if(item.type=='USER'){
      if(item.name=='firstName')
        return data.profile.firstName;
      if(item.name=='lastName')
        return data.profile.lastName;
      if(item.name=='email')
        return data.profile.email;
    }
    return data[item.name];
  }

  /**
   * Add custom options to the node menu (loaded via config)
   * @param toast
   * @param http
   * @param connector
   * @param custom
   * @param nodesIn
   * @param options
   * @param progressCallback
   */
  public static applyCustomNodeOptions(toast:Toast, http:Http, connector:RestConnectorService, custom: any, nodesIn: Node[], options: OptionItem[], progressCallback:Function,replaceUrl:any={}) {
    if (custom) {
      for (let c of custom) {
        if(c.remove){
          let i=Helper.indexOfObjectArray(options,'name',c.name)
          if(i!=-1)
            options.splice(i,1);
          continue;
        }
        if(c.mode=='nodes' && (!nodesIn || nodesIn.length))
          continue;
        if(c.mode=='noNodes' && nodesIn && nodesIn.length)
          continue;
        if (c.mode=='nodes' && c.isDirectory != 'any' && nodesIn && c.isDirectory != nodesIn[0].isDirectory)
          continue;
        if (!c.multiple && nodesIn && nodesIn.length > 1)
          continue;
        let position = c.position;
        if (c.position < 0)
          position = options.length - c.position;
        let item = new OptionItem(c.name, c.icon, (node: Node) => {
          let nodes = node == null ? nodesIn : [node];
          let ids = "";
          if(nodes) {
            for (let node of nodes) {
              if (ids)
                ids += ",";
              ids += node.ref.id;
            }
          }
          let url = c.url.replace(":id", ids)
          url = url.replace(":api", connector.getAbsoluteEndpointUrl());
          if(replaceUrl){
            for(let key in replaceUrl){
              url = url.replace(key,encodeURIComponent(replaceUrl[key]));
            }
          }
          if (!c.ajax) {
            window.open(url);
            return;
          }
          progressCallback(true);
          http.get(url).map((response: Response) => response.json()).subscribe((data: any) => {
            if (data.success)
              toast.toast(data.success, null, data.message ? data.success : data.message, data.message);
            else if (data.error)
              toast.error(null, data.error, null, data.message ? data.error : data.message, data.message);
            else
              toast.error(null);
            progressCallback(false);
          }, (error: any) => {
            toast.error(error);
            progressCallback(false);
          });
        });
        item.isSeperate = c.isSeperate;
        if (c.permission) {
          item.isEnabled = NodeHelper.getNodesRight(nodesIn, c.permission);
        }
        options.splice(position, 0, item);
      }

    }
  }

  /**
   * Apply (redirect url) node for usage by LMS systems
   * @param router
   * @param node
   */
  static addNodeToLms(router:Router,storage:TemporaryStorageService,node: Node,reurl:string) {
      storage.set(TemporaryStorageService.APPLY_TO_LMS_PARAMETER_NODE,node);
      router.navigate([UIConstants.ROUTER_PREFIX+"apply-to-lms",node.ref.repo, node.ref.id],{queryParams:{reurl:reurl}});
  }
  /**
   * Download one or multiple nodes
   * @param node
   */
  static downloadNodes(connector:RestConnectorService,nodes: Node[]) {
    if(nodes.length==1)
      return this.downloadNode(nodes[0]);
    let nodesString=RestHelper.getNodeIds(nodes).join(",");
      window.open(connector.getAbsoluteEndpointUrl()+
      "../eduservlet/download?appId="+
      encodeURIComponent(nodes[0].ref.repo)+
      "&nodeIds="+encodeURIComponent(nodesString));
  }

  static getLRMIProperty(data: any, item: ListItem) {
    // http://dublincore.org/dcx/lrmi-terms/2014-10-24/
    if(item.type=='NODE'){
      if(item.name==RestConstants.CM_NAME || item.name==RestConstants.CM_PROP_TITLE){
        return "name";
      }
      if(item.name==RestConstants.CM_CREATOR){
        return "author";
      }
      if(item.name==RestConstants.CM_PROP_C_CREATED){
        return "dateCreated";
      }
    }
    return "";
  }
  static getLRMIAttribute(translate:TranslateService,config:ConfigurationService,data: any, item: ListItem) {
    // http://dublincore.org/dcx/lrmi-terms/2014-10-24/
    if(item.type=='NODE'){
      if(item.name==RestConstants.CM_PROP_C_CREATED || item.name==RestConstants.CM_MODIFIED_DATE){
        return data.properties[item.name+'ISO8601'];
      }
    }
    return NodeHelper.getAttribute(translate,config,data,item);
  }

  public static getNodePositionInArray(search: Node, _nodes: Node[]) {
    let i=0;
    for(let node of _nodes) {
      if(node.ref) {
        if (node.ref.id == search.ref.id)
          return i;
      }
      i++;
    }
    return _nodes.indexOf(search);
  }

  public static getLicenseHtml(translate:TranslateService,data:Node) {
    return '<span title="'+NodeHelper.getLicenseName(data,translate)+'"><img alt="'+NodeHelper.getLicenseName(data,translate)+'" src="'+NodeHelper.getLicenseIcon(data)+'"></span>';
  }
  public static getSourceIconRepoPath(repo:Repository) {
    if(repo.isHomeRepo)
      return NodeHelper.getSourceIconPath('home');
    return NodeHelper.getSourceIconPath(repo.repositoryType.toLowerCase());
  }
  public static getSourceIconPath(src: string) {
    return 'assets/images/sources/' + src + '.png';
  }
  public static getWorkflowStatusById(config:ConfigurationService,id:string) : WorkflowDefinition{
    let workflows=NodeHelper.getWorkflows(config);
    let pos=Helper.indexOfObjectArray(workflows,'id',id);
    if(pos==-1) pos=0;
    let workflow=workflows[pos];
    return workflow;
  }
  public static getWorkflowStatus(config:ConfigurationService,node:Node) : WorkflowDefinition{
    let value=node.properties[RestConstants.CCM_PROP_WF_STATUS];
    if(value) value=value[0];
    if(!value)
      return NodeHelper.getWorkflows(config)[0];
   return NodeHelper.getWorkflowStatusById(config,value);
  }
  static getWorkflows(config: ConfigurationService) : WorkflowDefinition[] {
    return config.instant("workflows",[
      RestConstants.WORKFLOW_STATUS_UNCHECKED,
      RestConstants.WORKFLOW_STATUS_TO_CHECK,
      RestConstants.WORKFLOW_STATUS_HASFLAWS,
      RestConstants.WORKFLOW_STATUS_CHECKED,
    ]);
  }
}

