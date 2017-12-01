import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import {RestNodeService} from "../rest/services/rest-node.service";
import {RestConstants} from "../rest/rest-constants";
import {RestHelper} from "../rest/rest-helper";
import {Toast} from "../ui/toast";
import {RestConnectorService} from "../rest/services/rest-connector.service";
export interface EventListener {
  onEvent(event:string, data:any) : void;
}
@Injectable()
export class FrameEventsService {

  public static EVENT_UPDATE_LOGIN_STATE="UPDATE_LOGIN_STATE";
  public static EVENT_USER_LOGGED_IN="USER_LOGGED_IN";
  public static EVENT_USER_LOGGED_OUT="USER_LOGGED_OUT";
  public static EVENT_VIEW_SWITCHED="VIEW_SWITCHED";
  public static EVENT_VIEW_OPENED="VIEW_OPENED";
  public static EVENT_NODE_FOLDER_OPENED="NODE_FOLDER_OPENED";
  public static EVENT_GLOBAL_SEARCH="GLOBAL_SEARCH";
  public static EVENT_CONTENT_HEIGHT="CONTENT_HEIGHT";
  public static EVENT_INVALIDATE_HEIGHT="INVALIDATE_HEIGHT";
  public static EVENT_SESSION_TIMEOUT="SESSION_TIMEOUT";
  public static EVENT_APPLY_NODE="APPLY_NODE";
  public static EVENT_NODE_SAVED="NODE_SAVED";
  public static EVENT_REST_RESPONSE="PARENT_REST_RESPONSE";

  public static INVALIDATE_HEIGHT_EVENTS=[
    FrameEventsService.EVENT_VIEW_SWITCHED,
    FrameEventsService.EVENT_VIEW_OPENED,
    FrameEventsService.EVENT_NODE_FOLDER_OPENED
    ];

  // incomming events
  public static EVENT_PARENT_SCROLL="PARENT_SCROLL";
  public static EVENT_PARENT_SEARCH="PARENT_SEARCH";
  public static EVENT_PARENT_ADD_NODE_URL="PARENT_ADD_NODE_URL";
  public static EVENT_PARENT_REST_REQUEST="PARENT_REST_REQUEST";
  public static EVENT_UPDATE_SESSION_TIMEOUT="UPDATE_SESSION_TIMEOUT";

  private eventListeners :EventListener[]=[];
  private windows: Window[]=[];

  constructor() {
    let t=this;
    window.addEventListener('message', function (event:any) {
      if (event.source!==window.self && event.data){
        t.eventListeners.forEach(function(listener:EventListener){
          listener.onEvent(event.data.event,event.data.data);
        });
      }
    }, false);
      setInterval(()=>{
        this.broadcastEvent(FrameEventsService.EVENT_CONTENT_HEIGHT,document.body.scrollHeight);
      },250);
  }

  /**
   * Add a window which should be notified (a handle returned by window.open)
   * @param window
   */
  public addWindow(window:Window){
    this.windows.push(window);
  }
  public addListener(listener:EventListener) : void {
    this.eventListeners.push(listener);
  }
  /**
   * sends a message to a parent view
   * @param event use one constant from the event list
   * @param message can be an object with more information
   */
  public broadcastEvent(event:string, data:any=null) {
    if(FrameEventsService.INVALIDATE_HEIGHT_EVENTS.indexOf(event)!=-1){
      this.broadcastEvent(FrameEventsService.EVENT_INVALIDATE_HEIGHT);
    }
    if (this.isRunningInFrame()) {
      window.parent.postMessage({event: event, data: data}, '*');
    } else {
      window.postMessage({event: event, data: data}, '*');
    }
    for(let w of this.windows){
      try {
        w.postMessage({event: event, data: data}, '*');
      }catch(e){
        // The window has may be closed
      }
    }
  }

  /**
   * checks if the actual script is running in a frame
   * @returns {boolean}
   */
  public isRunningInFrame() : boolean {
    try {
      return window.self !== window.top;
    } catch (e) {
      return true;
    }
  }

}
