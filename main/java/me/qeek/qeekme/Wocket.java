/**
 * Created by Lucas on 29/06/2015.
 */

package me.qeek.qeekme;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import java.net.URI;

public class Wocket {
    //public delegate void Callback(params string[] args);
    HashMap<String, EventHandler> events;

    WebSocketClient wSocket = null;

    public WebSocket.READYSTATE GetReadyState() {
        if(wSocket == null)    //If the socket has not been initiated,
            return WebSocket.READYSTATE.CLOSED;   //return CLOSED state
        else //if it has,
            return wSocket.getReadyState();   //return the current state
    }

    public Wocket(){
        events = new HashMap<>();   //inits the map to store the methods assigned
    }

    public void Connect(String serverAddr) {

        //if the connection has already been initiated, return
        if(wSocket != null) return;

        URI uri;

        try {

            uri = new URI(serverAddr);

            wSocket = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    try {
                        if (events.containsKey("connected"))    //if the connected event is signed,
                            events.get("connected").Fire(null);
                    } catch(Exception ex) {
                        throwError(ex);
                    }
                }

                @Override
                public void onMessage(String message) {
                    try
                    {
                        //get the data obj from the data message received
                        WocketMessage dataObj = getDataObject(message);

                        //verifies whether the dataObj.event is not present, if any of them are protected  and if there is not callback sign with that value
                        if (dataObj == null || dataObj.eventName == "close" || dataObj.eventName == "connected" ||
                                dataObj.eventName == "error" || !events.containsKey(dataObj.eventName))
                            return; //if so, return

                        //if it pass all restrictions, execute the method
                        if (events.containsKey(dataObj.eventName))    //if the event is signed,
                            events.get(dataObj.eventName).Fire(dataObj.args);   //fire it
                    }
                    catch(Exception ex)
                    {
                        throwError(ex);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (events.containsKey("close"))    //if the close event is signed,
                        events.get("close").Fire(new String[1]);   //fire it
                }

                @Override
                public void onError(Exception ex) {
                    throwError(ex);  //throw error methods
                }
            };

            wSocket.connect();  //after everything has been set, connects the websocket

        } catch (Exception ex) {
            throwError(ex);
        }

    }

    public void On(String eventName, EventHandler callback) //sign callback event
    {
        if (!events.containsKey(eventName))  //if the event specified is still empty
            events.put(eventName, callback); // Inits the event name
    }

    public void Clear(String eventName)
    {
        if (events.containsKey(eventName))  //if the event exists
            events.remove(eventName);
    }

    public void Emit(String eventName, String... argList)
    {
        try
        {
            //this is needed due to send while not connected do not throw exceptions
            if (wSocket == null || wSocket.getReadyState() != WebSocket.READYSTATE.OPEN) //check if the socket is opened
                throw new Exception("emitFailedSocketNotOpen");  //if not, throw an error socket not open

            //create the data object with the data passed
            WocketMessage dataObj = new WocketMessage();
            dataObj.eventName = eventName;
            dataObj.args = argList;

            //create dataString
            String dataString = getDataString(dataObj);

            wSocket.send(dataString);    //send the data string
        }
        catch (Exception ex)
        {
            throwError(ex);  //throw error methods
        }
    }

    public void Close()
    {
        //must verify what else is needed to close the connection
        //and verify if once this method is called, the onclose method is automatically called aswell or we need to force its call

        if (wSocket != null && wSocket.getReadyState() == WebSocket.READYSTATE.CLOSED) //if it is not connected
            wSocket.close();   //close the socket connection
    }

    private void throwError(Exception ex)
    {
        if (events.containsKey("error"))    //if the error event is signed,
            events.get("error").Fire(new String[]{ex.getMessage()});   //fire it
    }

    private WocketMessage getDataObject(String jsonString) {
        try {
            JSONObject jo = new JSONObject(jsonString); //get the json obj from the string
            JSONArray ja = jo.getJSONArray("args");  //get the json array from json object

            WocketMessage wocketMessage = new WocketMessage();  //inits new wocket message
            wocketMessage.eventName = jo.getString("event");    //get the event from the json obj
            wocketMessage.args = new String[ja.length()];    //inits args array

            for(int i=0;i<ja.length();i++)  //iterate thru json array and copy all members
                wocketMessage.args[i] = ja.getString(i);

            return wocketMessage;   //return the wocketMessage obj

        } catch (Exception ex) {
            return null;    //if something fails, return null
        }
    }

    private String getDataString(WocketMessage jsonObject) {
        try {

            JSONObject jo = new JSONObject();   //creates new json obj
            JSONArray ja = new JSONArray(); //creates new json array

            //populate json array
            for(int i = 0 ;  i < jsonObject.args.length ; i++)
                ja.put(jsonObject.args[i]);

            jo.put("event", jsonObject.eventName);  //set event of json obj
            jo.put("args", ja); //set args of json obj

            return jo.toString();   //return json string

        } catch(Exception ex) {
            return "";  //if something fails, return empty
        }
    }
}

abstract class EventHandler {
    public abstract void Fire(String[] args);
}

class WocketMessage
{
    public String eventName;
    public String[] args;
}
