package com.randdusing.bluetoothle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.util.Log;
import android.util.Base64;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//TODO Verify how onResume and onPause events work

public class BluetoothLePlugin extends CordovaPlugin 
{
  //Logging related variables
  //private final static String TAG = BluetoothLePlugin.class.getSimpleName();
  
  //Callback variables
  private CallbackContext initCallbackContext;
  private CallbackContext scanCallbackContext;
  private CallbackContext connectCallbackContext;
  private CallbackContext operationCallbackContext;
  
  //Initialization related variables
  private final int REQUEST_BT_ENABLE = 59627; /*Random integer*/
  private BluetoothAdapter bluetoothAdapter;

  //Connection related variables
  private BluetoothGatt bluetoothGatt;
  private int connectionState = BluetoothProfile.STATE_DISCONNECTED;
  
  //Discovery related variables
  private final int STATE_UNDISCOVERED = 0;
  private final int STATE_DISCOVERING = 1;
  private final int STATE_DISCOVERED = 2;
  private int discoveredState = STATE_UNDISCOVERED;
  
  //Action Name Strings
  private final String initializeActionName = "initialize";
  private final String startScanActionName = "startScan";
  private final String stopScanActionName = "stopScan";
  private final String connectActionName = "connect";
  private final String reconnectActionName = "reconnect";
  private final String disconnectActionName = "disconnect";
  private final String closeActionName = "close";
  private final String discoverActionName = "discover";
  private final String servicesActionName = "services";
  private final String characteristicsActionName = "characteristics";
  private final String descriptorsActionName = "descriptors";
  private final String readActionName = "read";
  private final String subscribeActionName = "subscribe";
  private final String unsubscribeActionName = "unsubscribe";
  private final String writeActionName = "write";
  private final String readDescriptorActionName = "readDescriptor";
  private final String writeDescriptorActionName = "writeDescriptor";
  private final String isInitializedActionName = "isInitialized";
  private final String isScanningActionName = "isScanning";
  private final String isDiscoveredActionName = "isDiscovered";
  private final String isConnectedActionName = "isConnected";
  
  //Object keys
  private final String keyStatus = "status";
  private final String keyError = "error";
  private final String keyMessage = "message";
  private final String keyName = "name";
  private final String keyAddress = "address";
  private final String keyRssi = "rssi";
  private final String keyServiceAssignedNumbers = "serviceAssignedNumbers";
  private final String keyServiceAssignedNumber = "serviceAssignedNumber";
  private final String keyCharacteristicAssignedNumber = "characteristicAssignedNumber";
  private final String keyDescriptorAssignedNumber = "descriptorAssignedNumber";
  private final String keyServices = "services";
  private final String keyCharacteristics = "characteristics";
  private final String keyDescriptors = "descriptors";
  private final String keyValue = "value";
  private final String keyIsInitialized = "isInitalized";
  private final String keyIsScanning = "isScanning";
  private final String keyIsConnected = "isConnected";
  private final String keyIsDiscovered = "isDiscovered";
  private final String keyIsNotification = "isNotification";
  
  //Status Types
  private final String statusInitialized = "initialized";
  private final String statusScanStarted = "scanStarted";
  private final String statusScanStopped = "scanStopped";
  private final String statusScanResult = "scanResult";
  private final String statusConnected = "connected";
  private final String statusConnecting = "connecting";
  private final String statusDisconnected = "disconnected";
  private final String statusDisconnecting = "disconnecting";
  private final String statusClosed = "closed";
  private final String statusDiscovered = "discovered";
  private final String statusRead = "read";
  private final String statusSubscribed = "subscribed";
  private final String statusSubscribedResult = "subscribedResult";
  private final String statusUnsubscribed = "unsubscribed";
  private final String statusWritten = "written";
  private final String statusReadDescriptor = "readDescriptor";
  private final String statusWrittenDescriptor = "writtenDescriptor";
  
  //Error Types
  private final String errorInitialize = "initialize";
  private final String errorArguments = "arguments";
  private final String errorStartScan = "startScan";
  private final String errorStopScan = "stopScan";
  private final String errorConnect = "connect";
  private final String errorReconnect = "reconnect";
  private final String errorDiscover = "discover";
  private final String errorRead = "read";
  private final String errorSubscription = "subscription";
  private final String errorWrite = "write";
  private final String errorReadDescriptor = "readDescriptor";
  private final String errorWriteDescriptor = "writeDescriptor";
  private final String errorNeverConnected = "neverConnected";
  private final String errorIsNotDisconnected = "isNotDisconnected";
  private final String errorIsNotConnected = "isNotConnected";
  private final String errorIsDisconnected = "isDisconnected";
  private final String errorService = "service";
  private final String errorCharacteristic = "characteristic";
  private final String errorDescriptor = "descriptor";
  
  //Error Messages
  //Initialization
  private final String logNotEnabled = "Bluetooth not enabled";
  private final String logNotEnabledUser = "Bluetooth not enabled by user";
  private final String logNotSupported = "Hardware doesn't support Bluetooth LE";
  private final String logNotInit = "Bluetooth not initialized";
  //Scanning
  private final String logAlreadyScanning = "Scanning already in progress";
  private final String logScanStartFail = "Scan failed to start";
  private final String logNotScanning = "Not scanning";
  //Connection
  private final String logPreviouslyConnected = "Device previously connected, reconnect or close for new device";
  private final String logNeverConnected = "Never connected to device";
  private final String logIsNotConnected = "Device isn't connected";
  private final String logIsNotDisconnected = "Device isn't disconnected";
  private final String logIsDisconnected = "Device is disconnected";
  private final String logNoAddress = "No device address";
  private final String logNoDevice = "Device not found";
  private final String logReconnectFail = "Reconnection to device failed";
  //Discovery
  private final String logAlreadyDiscovering = "Already discovering device";
  private final String logDiscoveryFail = "Unable to discover device";
  //Read/write
  private final String logNoArgObj = "Argument object not found";
  private final String logNoService = "Service not found";
  private final String logNoCharacteristic = "Characteristic not found";
  private final String logNoDescriptor = "Descriptor not found";
  private final String logReadFail = "Unable to read";
  private final String logReadFailReturn = "Unable to read on return";
  private final String logSubscribeFail = "Unable to subscribe";
  private final String logUnsubscribeFail = "Unable to unsubscribe";
  private final String logWriteFail = "Unable to write";
  private final String logWriteFailReturn = "Unable to write on return";
  private final String logWriteValueNotFound = "Write value not found";
  private final String logWriteValueNotSet = "Write value not set";
  private final String logReadDescriptorFail = "Unable to read descriptor";
  private final String logReadDescriptorFailReturn = "Unable to read descriptor on return";
  private final String logWriteDescriptorNotAllowed = "Unable to write client configuration descriptor";
  private final String logWriteDescriptorFail = "Unable to write descriptor";
  private final String logWriteDescriptorValueNotFound = "Write descriptor value not found";
  private final String logWriteDescriptorValueNotSet = "Write descriptor value not set";
  private final String logWriteDescriptorFailReturn = "Descriptor not written on return";
  
  //Base for UUIDs
  private final String uuidBase = "0000%s-0000-1000-8000-00805f9b34fb";
  
  //Client Configuration UUID for notifying/indicating
  private final UUID clientConfigurationDescriptorUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  //Actions
  @Override
  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException
  {
    //Execute the specified action
    if (initializeActionName.equals(action))
    {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          initializeAction(callbackContext);
        }
      });
      return true;
    }
    else if (startScanActionName.equals(action))
    {
      startScanAction(args, callbackContext);
      return true;
    }
    else if (stopScanActionName.equals(action)) 
    {
      stopScanAction(callbackContext);
      return true;
    }
    else if (connectActionName.equals(action))
    {
      connectAction(args, callbackContext);
      return true;
    }
    else if (reconnectActionName.equals(action))
    {
      reconnectAction(callbackContext);
      return true;
    }
    else if (disconnectActionName.equals(action))
    {
      disconnectAction(callbackContext);
      return true;      
    }
    else if (servicesActionName.equals(action))
    {
      callbackContext.success();
      return true;      
    }
    else if (characteristicsActionName.equals(action))
    {
      callbackContext.success();
      return true;      
    }
    else if (descriptorsActionName.equals(action))
    {
      callbackContext.success();
      return true;      
    }
    else if (closeActionName.equals(action))
    {
      closeAction(callbackContext);
      return true;
    }
    else if (discoverActionName.equals(action))
    {
      discoverAction(callbackContext);
      return true;
    }
    else if (readActionName.equals(action))
    {
      readAction(args, callbackContext);
      return true;
    }
    else if (subscribeActionName.equals(action))
    {
      subscribeAction(args, callbackContext);
      return true;
    }
    else if (unsubscribeActionName.equals(action))
    {
      unsubscribeAction(args, callbackContext);
      return true;
    }
    else if (writeActionName.equals(action))
    {
      writeAction(args, callbackContext);
      return true;
    }
    else if (readDescriptorActionName.equals(action))
    {
      readDescriptorAction(args, callbackContext);
      return true;
    }
    else if (writeDescriptorActionName.equals(action))
    {
      writeDescriptorAction(args, callbackContext);
      return true;
    }
    else if (isInitializedActionName.equals(action))
    {
    	isInitializedAction(callbackContext);
    	return true;
    }
    else if (isScanningActionName.equals(action))
    {
    	isScanningAction(callbackContext);
    	return true;
    }
    else if (isConnectedActionName.equals(action))
    {
      isConnectedAction(callbackContext);
      return true;
    }
    else if (isDiscoveredActionName.equals(action))
    {
      isDiscoveredAction(callbackContext);
      return true;
    }
    return false;
  }
  
  private void initializeAction(CallbackContext callbackContext)
  {
    JSONObject returnObj = new JSONObject();
    
    //If Bluetooth is already enabled, return success
    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
    {
    	addProperty(returnObj, keyStatus, statusInitialized);
	    callbackContext.success(returnObj);
	    return;
    }
    
    //Check whether the device supports Bluetooth LE
    //Not necessary if app manifest contains: <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
    if (!cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
    {
    	addProperty(returnObj, keyError, errorInitialize);
    	addProperty(returnObj, keyMessage, logNotSupported);
      callbackContext.error(returnObj);
      return;
    }
    
    //Get Bluetooth adapter via Bluetooth Manager
    BluetoothManager bluetoothManager = (BluetoothManager) cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();

    //If adapter is null or disabled...
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
    {
      //Request Bluetooth to be enabled
      initCallbackContext = callbackContext;
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      cordova.startActivityForResult(this, enableBtIntent, REQUEST_BT_ENABLE);
    }
    //Else successful
    else
    {
    	addProperty(returnObj, keyStatus, statusInitialized);
	    callbackContext.success(returnObj);
	    return;
    }
  }
  
  private void startScanAction(JSONArray args, CallbackContext callbackContext)
  {
  	if (isNotInitialized(callbackContext))
  	{
  		return;
  	}
  	
  	JSONObject returnObj = new JSONObject();
    
    //If the adapter is already scanning, don't call another scan.
    if (scanCallbackContext != null)
    {
    	addProperty(returnObj, keyError, errorStartScan);
    	addProperty(returnObj, keyMessage, logAlreadyScanning);
      callbackContext.error(returnObj);
      return;
    }
    
    //Get the service UUIDs from the arguments
    JSONObject obj = getArgsObject(args);
    
    UUID[] serviceUuids = null;
    
    if (obj != null)
    {
      serviceUuids = getServiceUuids(obj);
    }
    
    //Save the callback context for reporting back found devices. Also the isScanning flag
    scanCallbackContext = callbackContext;

    //Start the scan with or without service UUIDs
    boolean result;
    if (serviceUuids == null || serviceUuids.length == 0)
    {
      result = bluetoothAdapter.startLeScan(scanCallback);
    }
    else
    {
      result = bluetoothAdapter.startLeScan(serviceUuids, scanCallback);
    }
    
    //If the scan didn't start...
    if (!result)
    {
    	addProperty(returnObj, keyError, errorStartScan);
    	addProperty(returnObj, keyMessage, logScanStartFail);
      callbackContext.error(returnObj);
      scanCallbackContext = null;
      return;
    }
    
    //Notify user of started scan and save callback
    addProperty(returnObj, keyStatus, statusScanStarted);
    
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
  }
  
  private void stopScanAction(CallbackContext callbackContext)
  {
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
  	JSONObject returnObj = new JSONObject();
    
    //Check if already scanning
    if (scanCallbackContext == null)
    {
    	addProperty(returnObj, keyError, errorStopScan);
    	addProperty(returnObj, keyMessage, logNotScanning);
      callbackContext.error(returnObj);
      return;
    }
    
    //Stop the scan
    bluetoothAdapter.stopLeScan(scanCallback);
    
    //Set scanning state
    scanCallbackContext = null;

    //Inform user
    addProperty(returnObj, keyStatus, statusScanStopped);
    callbackContext.success(returnObj);
  }

  private void connectAction(JSONArray args, CallbackContext callbackContext)
  { 
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
  	JSONObject returnObj = new JSONObject();
    
    if (bluetoothGatt != null)
    {
      addProperty(returnObj, keyError, errorConnect);
      addProperty(returnObj, keyMessage, logPreviouslyConnected);
      callbackContext.error(returnObj);
      return;
    }
    
    //Get the address string
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    //Get address
    String address = getAddress(obj);
    
    if (address == null)
    {
    	addProperty(returnObj, keyError, errorConnect);
    	addProperty(returnObj, keyMessage, logNoAddress);
      callbackContext.error(returnObj);
      return;
    }
    
    //Get the device
    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
    
    //If device wasn't found...
    if (device == null)
    {
    	addProperty(returnObj, keyError, errorConnect);
    	addProperty(returnObj, keyMessage, logNoDevice);
      callbackContext.error(returnObj);
      return;
    }
    
    //Connect!
    connectCallbackContext = callbackContext;
    connectionState = BluetoothProfile.STATE_CONNECTING;
    bluetoothGatt = device.connectGatt(cordova.getActivity().getApplicationContext(), false, gattCallback);

    //Return connecting status
    addProperty(returnObj, keyStatus, statusConnecting);
    addProperty(returnObj, keyName, device.getName());
    addProperty(returnObj, keyAddress, device.getAddress());
    
    //Keep the callback
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
  }
   
  private void reconnectAction(CallbackContext callbackContext)
  {
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
    if (wasNeverConnected(callbackContext))
    {
      return;
    }
    
    if (isNotDisconnected(callbackContext))
    {
      return;
    }
    
    JSONObject returnObj = new JSONObject();
    
    connectCallbackContext = callbackContext;
    
    boolean result = bluetoothGatt.connect();
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorReconnect);
    	addProperty(returnObj, keyMessage, logReconnectFail);
      callbackContext.error(returnObj);
      connectCallbackContext = null;
      return;
    }
    
    connectionState = BluetoothProfile.STATE_CONNECTING;
    
    BluetoothDevice device = bluetoothGatt.getDevice();
    
    //Return connecting status and keep callback
    addProperty(returnObj, keyStatus, statusConnecting);
    addProperty(returnObj, keyName, device.getName());
    addProperty(returnObj, keyAddress, device.getAddress());
    
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
  }
  
  private void disconnectAction(CallbackContext callbackContext)
  {
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
    if (wasNeverConnected(callbackContext))
    {
      return;
    }
    
    if (isDisconnected(callbackContext))
    {
      return;
    }
    
  	JSONObject returnObj = new JSONObject();
  	
    BluetoothDevice device = bluetoothGatt.getDevice();
    
    //Return disconnecting status and keep callback
    addProperty(returnObj, keyStatus, statusDisconnecting);
    addProperty(returnObj, keyName, device.getName());
    addProperty(returnObj, keyAddress, device.getAddress());
    
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
    
    //Call disconnect and change connection station
    connectionState = BluetoothProfile.STATE_DISCONNECTING;
    connectCallbackContext = callbackContext;
    bluetoothGatt.disconnect();
  }

  private void closeAction(CallbackContext callbackContext)
  {  
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
    if (wasNeverConnected(callbackContext))
    {
      return;
    }
    
    if (isNotDisconnected(callbackContext))
    {
      return;
    }
    
    JSONObject returnObj = new JSONObject();
    
    BluetoothDevice device = bluetoothGatt.getDevice();
    
    addProperty(returnObj, keyStatus, statusClosed);
    addProperty(returnObj, keyAddress, device.getAddress());
    addProperty(returnObj, keyName, device.getName());
    
    bluetoothGatt.close();
    bluetoothGatt = null;
    
    discoveredState = STATE_UNDISCOVERED;

    connectCallbackContext = null;
    operationCallbackContext = null;
    
    callbackContext.success(returnObj);
  }
  
  private void discoverAction(CallbackContext callbackContext)
  {
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
    if (wasNeverConnected(callbackContext))
    {
      return;
    }
     
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject returnObj = new JSONObject();
    
    //Already initiated discovery
    if (discoveredState == STATE_DISCOVERING)
    {
    	addProperty(returnObj, keyError, errorDiscover);
    	addProperty(returnObj, keyMessage, logAlreadyDiscovering);
      callbackContext.error(returnObj);
      return;
    }
    //Already discovered
    else if (discoveredState == STATE_DISCOVERED)
    {
      returnObj = getDiscovery();
      callbackContext.success(returnObj);
      return;
    }
    
    //Else undiscovered, so start discovery
    discoveredState = STATE_DISCOVERING;
    operationCallbackContext = callbackContext;
    bluetoothGatt.discoverServices();
  }

  private void readAction(JSONArray args, CallbackContext callbackContext)
  {
  	if (isNotInitialized(callbackContext))
    {
    	return;
    }
  	
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
    
    operationCallbackContext = callbackContext;
    
    boolean result = bluetoothGatt.readCharacteristic(characteristic);
    
    if (!result)
    {
    	JSONObject returnObj = new JSONObject();
    	addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
    	addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
    	addProperty(returnObj, keyError, errorRead);
    	addProperty(returnObj, keyMessage, logReadFail);
      callbackContext.error(returnObj);
      operationCallbackContext = null;
      return;
    }
  }
   
  private void subscribeAction(JSONArray args, CallbackContext callbackContext)
  {
    if (isNotInitialized(callbackContext))
    {
    	return;
    }
    
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    boolean isNotification = obj.optBoolean(keyIsNotification, true);
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
    
    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(clientConfigurationDescriptorUuid);
    
    if (isNotDescriptor(descriptor, callbackContext))
    {
    	return;
    }
    
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
  	addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
  	
  	//Subscribe to the characteristic
    boolean result = bluetoothGatt.setCharacteristicNotification(characteristic, true);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorSubscription);
    	addProperty(returnObj, keyMessage, logSubscribeFail);
      callbackContext.error(returnObj);
      return;
    }
    
    
    //Set the descriptor for notification
    if (isNotification)
    {
    	result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }
    //Or for indication
    else
    {
    	result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
    }
    
  	if (!result)
  	{
  		//Clean up
  		bluetoothGatt.setCharacteristicNotification(characteristic, false);
  		
  		addProperty(returnObj, keyError, errorWriteDescriptor);
  		addProperty(returnObj, keyMessage, logWriteDescriptorValueNotSet);
  		callbackContext.error(returnObj);
  		return;
  	}
  	
    operationCallbackContext = callbackContext;
  	
    //Write the descriptor value
  	result = bluetoothGatt.writeDescriptor(descriptor);
  	
  	if (!result)
  	{
  		//Clean up
  		bluetoothGatt.setCharacteristicNotification(characteristic, false);

  		addProperty(returnObj, keyError, errorWriteDescriptor);
  		addProperty(returnObj, keyMessage, logWriteDescriptorFail);
  		callbackContext.error(returnObj);
      operationCallbackContext = null;
  	}
  }
  
  private void unsubscribeAction(JSONArray args, CallbackContext callbackContext)
  {
  	JSONObject returnObj = new JSONObject();
  	
  	if (isNotInitialized(callbackContext))
    {
    	return;
    }
  	
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
    
    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(clientConfigurationDescriptorUuid);
    
    if (isNotDescriptor(descriptor, callbackContext))
    {
    	return;
    }

  	addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
  	addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
  	
  	//Unsubscribe to the characteristic
    boolean result = bluetoothGatt.setCharacteristicNotification(characteristic, false);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorSubscription);
    	addProperty(returnObj, keyMessage, logUnsubscribeFail);
      callbackContext.error(returnObj);
      return;
    }
    
    //Set the descriptor for disabling notification/indication
    result = descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
      
  	if (!result)
  	{
  		addProperty(returnObj, keyError, errorWriteDescriptor);
  		addProperty(returnObj, keyMessage, logWriteDescriptorValueNotSet);
  		callbackContext.error(returnObj);
  		return;
  	}
  	 
  	operationCallbackContext = callbackContext;
  	
    //Write the actual descriptor value
  	result = bluetoothGatt.writeDescriptor(descriptor);
  	
  	if (!result)
  	{
  		addProperty(returnObj, keyError, errorWriteDescriptor);
  		addProperty(returnObj, keyMessage, logWriteDescriptorFail);
  		callbackContext.error(returnObj);
      operationCallbackContext = null;
  	}
  }

  private void writeAction(JSONArray args, CallbackContext callbackContext)
  {
  	if (isNotInitialized(callbackContext))
    {
    	return;
    }
  	
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
    
  	JSONObject returnObj = new JSONObject();
  	addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
  	addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
  	
    byte[] value = getValue(obj);
    
    if (value == null)
    {
    	addProperty(returnObj, keyError, errorWrite);
    	addProperty(returnObj, keyMessage, logWriteValueNotFound);
      callbackContext.error(returnObj);
      return;
    }
    
    boolean result = characteristic.setValue(value);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorWrite);
    	addProperty(returnObj, keyMessage, logWriteValueNotSet);
      callbackContext.error(returnObj);
      return;
    }
    
    operationCallbackContext = callbackContext;
    
    result = bluetoothGatt.writeCharacteristic(characteristic);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorWrite);
    	addProperty(returnObj, keyMessage, logWriteFail);
      callbackContext.error(returnObj);
      operationCallbackContext = null;
      return;
    }
  }
  
  private void readDescriptorAction(JSONArray args, CallbackContext callbackContext)
  {
  	if (isNotInitialized(callbackContext))
    {
    	return;
    }
  	
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
  	
    BluetoothGattDescriptor descriptor = getDescriptor(obj, characteristic);
    
    if (isNotDescriptor(descriptor, callbackContext))
    {
    	return;
    }
    
    operationCallbackContext = callbackContext;
    
    boolean result = bluetoothGatt.readDescriptor(descriptor);
    
    if (!result)
    {
      JSONObject returnObj = new JSONObject();
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
      addProperty(returnObj, keyDescriptorAssignedNumber, getAssignedNumber(descriptor.getUuid()));
    	addProperty(returnObj, keyError, errorReadDescriptor);
    	addProperty(returnObj, keyMessage, logReadDescriptorFail);
      callbackContext.error(returnObj);
      operationCallbackContext = null;
      return;
    }
  }
  
  private void writeDescriptorAction(JSONArray args, CallbackContext callbackContext)
  {
  	if (isNotInitialized(callbackContext))
    {
    	return;
    }
  	
    if (isNotConnected(callbackContext))
    {
    	return;
    }
    
    JSONObject obj = getArgsObject(args);
    
    if (isNotArgsObject(obj, callbackContext))
    {
      return;
    }
    
    BluetoothGattService service = getService(obj);
    
    if (isNotService(service, callbackContext))
    {
    	return;
    }
    
    BluetoothGattCharacteristic characteristic = getCharacteristic(obj, service);
    
    if (isNotCharacteristic(characteristic, callbackContext))
    {
    	return;
    }
  	
    BluetoothGattDescriptor descriptor = getDescriptor(obj, characteristic);
    
    if (isNotDescriptor(descriptor, callbackContext))
    {
    	return;
    }
    
  	JSONObject returnObj = new JSONObject();
    
    addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
  	addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
  	addProperty(returnObj, keyDescriptorAssignedNumber, getAssignedNumber(descriptor.getUuid()));
  	
  	//Let subscribe/unsubscribe take care of it
    if (descriptor.getUuid().equals(clientConfigurationDescriptorUuid))
    {
    	addProperty(returnObj, keyError, errorWriteDescriptor);
    	addProperty(returnObj, keyMessage, logWriteDescriptorNotAllowed);
    	callbackContext.error(returnObj);
    	return;
    }
    
    byte[] value = getValue(obj);
    
    if (value == null)
    {
    	addProperty(returnObj, keyError, errorWriteDescriptor);
    	addProperty(returnObj, keyMessage, logWriteDescriptorValueNotFound);
      callbackContext.error(returnObj);
      return;
    }
    
    boolean result = descriptor.setValue(value);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorWriteDescriptor);
    	addProperty(returnObj, keyMessage, logWriteDescriptorValueNotSet);
      callbackContext.error(returnObj);
      return;
    }
    
    operationCallbackContext = callbackContext;
    
    result = bluetoothGatt.writeDescriptor(descriptor);
    
    if (!result)
    {
    	addProperty(returnObj, keyError, errorWriteDescriptor);
    	addProperty(returnObj, keyMessage, logWriteDescriptorFail);
      callbackContext.error(returnObj);
      operationCallbackContext = null;
      return;
    }
  }
  
  private void isInitializedAction(CallbackContext callbackContext)
  {
  	boolean result = (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
  	
    JSONObject returnObj = new JSONObject();
  	addProperty(returnObj, keyIsInitialized, result);
  	
    callbackContext.success(returnObj);
  }
  
  private void isScanningAction(CallbackContext callbackContext)
  {
  	boolean result = (scanCallbackContext != null);
  	
    JSONObject returnObj = new JSONObject();
  	addProperty(returnObj, keyIsScanning, result);
  	
  	callbackContext.success(returnObj);
  }
  
  private void isConnectedAction(CallbackContext callbackContext)
  {
  	boolean result = (connectionState == BluetoothAdapter.STATE_CONNECTED);
    
  	JSONObject returnObj = new JSONObject();
  	addProperty(returnObj, keyIsConnected, result);
  	
  	callbackContext.success(returnObj);
  }
  
  private void isDiscoveredAction(CallbackContext callbackContext)
  {
  	boolean result = (discoveredState == STATE_DISCOVERED);
    
  	JSONObject returnObj = new JSONObject();
  	addProperty(returnObj, keyIsDiscovered, result);
  	
  	callbackContext.success(returnObj);
  }

  //Enable Bluetooth Callback
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    //If this was a Bluetooth enablement request...
    if (requestCode == REQUEST_BT_ENABLE)
    {
    	//If callback doesnt exist, no reason to proceed
    	if (initCallbackContext == null)
    	{
    		return;
    	}
    	
    	JSONObject returnObj = new JSONObject();
    	
      //If Bluetooth was enabled...
      if (resultCode == Activity.RESULT_OK)
      {
        //After requesting, check again whether it's enabled
        BluetoothManager bluetoothManager = (BluetoothManager) cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        //Bluetooth wasn't enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
        	addProperty(returnObj, keyError, errorInitialize);
        	addProperty(returnObj, keyMessage, logNotEnabled);
          
    	    initCallbackContext.error(returnObj);
        }
        //Bluetooth was enabled
        else
        {
        	addProperty(returnObj, keyStatus, statusInitialized);
    	    initCallbackContext.success(returnObj);
        }
      }
      //Else user didn't enable Bluetooth
      else
      {
      	addProperty(returnObj, keyError, errorInitialize);
      	addProperty(returnObj, keyMessage, logNotEnabledUser);
      	
  	    initCallbackContext.error(returnObj);
      }
      
      initCallbackContext = null;
    }
  }
  
  //Scan Callback
  private LeScanCallback scanCallback = new LeScanCallback()
  {
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
    {
    	if (scanCallbackContext == null)
    	{
    		return;
    	}
    	
      JSONObject returnObj = new JSONObject();
      
      addProperty(returnObj, keyName, device.getName());
      addProperty(returnObj, keyAddress, device.getAddress());
      addProperty(returnObj, keyRssi, rssi);
      addProperty(returnObj, keyStatus, statusScanResult);
      
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
      pluginResult.setKeepCallback(true);
      scanCallbackContext.sendPluginResult(pluginResult);
    }
  };
  
  //Bluetooth callback for connecting, discovering, reading and writing
  private final BluetoothGattCallback gattCallback =  new BluetoothGattCallback()
  {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
      //Get the connected device
      BluetoothDevice device = gatt.getDevice();
      
      connectionState = newState;
      
      //Device was connected
      if (newState == BluetoothProfile.STATE_CONNECTED)
      {
        //This shouldn't happen
        if (connectCallbackContext == null)
        {
          return;
        }

	      //Create json object with address, name and connection status
	      JSONObject returnObj = new JSONObject();
	      addProperty(returnObj, keyStatus, statusConnected);
	      addProperty(returnObj, keyAddress, device.getAddress());
	      addProperty(returnObj, keyName, device.getName());

	      //Keep connection call back for disconnect
	      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
	      pluginResult.setKeepCallback(true);
	      connectCallbackContext.sendPluginResult(pluginResult);
      }
      //Device was disconnected
      else if (newState == BluetoothProfile.STATE_DISCONNECTED)
      {      
        operationCallbackContext = null;

        if (connectCallbackContext == null)
        {
          return;
        }
        
        JSONObject returnObj = new JSONObject();
        addProperty(returnObj, keyStatus, statusDisconnected);
        addProperty(returnObj, keyAddress, device.getAddress());
        addProperty(returnObj, keyName, device.getName());
        
        connectCallbackContext.success(returnObj);
        connectCallbackContext = null;
      }
    }
  
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
    	if (status == BluetoothGatt.GATT_SUCCESS)
    	{
    		discoveredState = STATE_DISCOVERED;
    	}
    	else
    	{
    		discoveredState = STATE_UNDISCOVERED;
    	}
    	
      //Shouldn't happen, but check for null callback
      if (operationCallbackContext == null)
      {
        return;
      }
      
      JSONObject returnObj = new JSONObject();
      
      //If successfully discovered, return list of services, characteristics and descriptors
      if (status == BluetoothGatt.GATT_SUCCESS)
      {
        returnObj = getDiscovery();
        operationCallbackContext.success(returnObj);
      }
      //Else it failed
      else
      {
      	addProperty(returnObj, keyError, errorDiscover);
	      addProperty(returnObj, keyMessage, logDiscoveryFail);
        operationCallbackContext.error(returnObj);
      }
      
      //Clear the callback
      operationCallbackContext = null;
    }
  
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
      //If no callback, just return
      if (operationCallbackContext == null)
      {
        return;
      }
      
      JSONObject returnObj = new JSONObject();
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(characteristic.getService().getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
      
      //If successfully read, return value
      if (status == BluetoothGatt.GATT_SUCCESS)
      {
	      addProperty(returnObj, keyStatus, statusRead);
	      addValue(returnObj, characteristic.getValue());
        operationCallbackContext.success(returnObj);
      }
      //Else it failed
      else
      {
      	addProperty(returnObj, keyError, errorRead);
	      addProperty(returnObj, keyMessage, logReadFailReturn);
        operationCallbackContext.error(returnObj);
      }
      
      //Clear callback
      operationCallbackContext = null;
    }
    
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
      //If callback is null, just return
      if (operationCallbackContext == null)
      {
        return;
      }
      
      JSONObject returnObj = new JSONObject();
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(characteristic.getService().getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));   
      addProperty(returnObj, keyStatus, statusSubscribedResult);
      addValue(returnObj, characteristic.getValue());

      //Return the characteristic value
      PluginResult result = new PluginResult(PluginResult.Status.OK, returnObj);
      result.setKeepCallback(true);
      operationCallbackContext.sendPluginResult(result);
    }
    
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
      //If no callback, just return
      if (operationCallbackContext == null)
      {
        return;
      }
      
      JSONObject returnObj = new JSONObject();
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(characteristic.getService().getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
      
      //If write was successful, return the written value
      if (status == BluetoothGatt.GATT_SUCCESS)
      {
        addProperty(returnObj, keyStatus, statusWritten);
        addValue(returnObj, characteristic.getValue());
        operationCallbackContext.success(returnObj);
      }
      //Else it failed
      else
      {
      	addProperty(returnObj, keyError, errorWrite);
        addProperty(returnObj, keyMessage, logWriteFailReturn);
        operationCallbackContext.error(returnObj);
      }
      
      //Clear callback
      operationCallbackContext = null;
    }
    
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
    {
      //If callback is null, just return
      if (operationCallbackContext == null)
      {
        return;
      }
      
    	BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
      
      JSONObject returnObj = new JSONObject();
      
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(characteristic.getService().getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));   
      addProperty(returnObj, keyDescriptorAssignedNumber, getAssignedNumber(descriptor.getUuid()));
      
      //If descriptor was successful, return the written value
      if (status == BluetoothGatt.GATT_SUCCESS)
      {
        addProperty(returnObj, keyStatus, statusReadDescriptor);
        addValue(returnObj, descriptor.getValue());
        operationCallbackContext.success(returnObj);
      }
      //Else it failed
      else
      {
      	addProperty(returnObj, keyError, errorReadDescriptor);
        addProperty(returnObj, keyMessage, logReadDescriptorFailReturn);
        operationCallbackContext.error(returnObj);
      }

      //Clear callback
      operationCallbackContext = null;
    }
  
    @Override
    public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
    {
    	//If callback is null, just return
      if (operationCallbackContext == null)
      {
        return;
      }
      
    	BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
      
      JSONObject returnObj = new JSONObject();
      
      addProperty(returnObj, keyServiceAssignedNumber, getAssignedNumber(characteristic.getService().getUuid()));
      addProperty(returnObj, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));  
      
      //See if notification/indication is enabled or disabled and use subscribe/unsubscribe callback instead
      if (descriptor.getUuid().equals(clientConfigurationDescriptorUuid))
      {
      	if (descriptor.getValue() == BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
      	{
      		addProperty(returnObj, keyStatus, statusUnsubscribed);
      		
      		operationCallbackContext.success(returnObj);
      	}
      	else
      	{
      		addProperty(returnObj, keyStatus, statusSubscribed);
      	  
      	  PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnObj);
      	  pluginResult.setKeepCallback(true);
      	  operationCallbackContext.sendPluginResult(pluginResult);
      	}

    	  return;
      }
      
      addProperty(returnObj, keyDescriptorAssignedNumber, getAssignedNumber(descriptor.getUuid()));
      
			//If descriptor was written, return written value
      if (status == BluetoothGatt.GATT_SUCCESS)
      {
      	addProperty(returnObj, keyStatus, statusWrittenDescriptor);
        addValue(returnObj, descriptor.getValue());
        operationCallbackContext.success(returnObj);
      }
      //Else it failed
      else
      {
      	addProperty(returnObj, keyError, errorWriteDescriptor);
        addProperty(returnObj, keyMessage, logWriteDescriptorFailReturn);
        operationCallbackContext.error(returnObj);
      }
      
      //Clear callback
      operationCallbackContext = null;
    }
  };
  
  //Helpers for BluetoothGatt classes
  private BluetoothGattService getService(JSONObject obj)
  {
    String uuidServiceValue = obj.optString(keyServiceAssignedNumber, null);
    
    if (uuidServiceValue == null)
    {
      return null;
    }
    
    String uuidServiceString = String.format(uuidBase, uuidServiceValue);
    
    UUID uuidService = null;
    
    try
    {
      uuidService = UUID.fromString(uuidServiceString);
    }
    catch (Exception ex)
    {
      return null;
    }
    
    BluetoothGattService service = bluetoothGatt.getService(uuidService);
    
    if (service == null)
    {
      return null;
    }
    
    return service;
  }
  
  private BluetoothGattCharacteristic getCharacteristic(JSONObject obj, BluetoothGattService service)
  { 
    String uuidCharacteristicValue = obj.optString(keyCharacteristicAssignedNumber, null);
    
    if (uuidCharacteristicValue == null)
    {
      return null;
    }
    
    String uuidCharacteristicString = String.format(uuidBase, uuidCharacteristicValue);
    
    UUID uuidCharacteristic = null;
    
    try
    {
      uuidCharacteristic = UUID.fromString(uuidCharacteristicString);
    }
    catch (Exception ex)
    {
      return null;
    }
    
    BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuidCharacteristic);
    
    if (characteristic == null)
    {
      return null;
    }
    
    return characteristic;
  }

  private BluetoothGattDescriptor getDescriptor(JSONObject obj, BluetoothGattCharacteristic characteristic)
  {
    String uuidDescriptorValue = obj.optString(keyDescriptorAssignedNumber, null);
    
    if (uuidDescriptorValue == null)
    {
      return null;
    }
    
    String uuidDescriptorString = String.format(uuidBase, uuidDescriptorValue);
    
    UUID uuidDescriptor = null;
    
    try
    {
      uuidDescriptor = UUID.fromString(uuidDescriptorString);
    }
    catch (Exception ex)
    {
      return null;
    }
    
    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuidDescriptor);
    
    if (descriptor == null)
    {
      return null;
    }
    
    return descriptor;
  }

  //Helpers to check conditions and send callbacks
  private boolean isNotInitialized(CallbackContext callbackContext)
  {
    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
    {
      return false;
    }
    
    JSONObject returnObj = new JSONObject();
    
    addProperty(returnObj, keyError, errorInitialize);
    addProperty(returnObj, keyMessage, logNotInit);
    
    callbackContext.error(returnObj);
    
    //Assumption that disabled Bluetooth adapter "kills" current scans, current connected devices, etc
    
    //Clean up callbacks
    initCallbackContext = null;
    scanCallbackContext = null;
    connectCallbackContext = null;
    operationCallbackContext = null;
    
    //Clean up states
    connectionState = BluetoothProfile.STATE_DISCONNECTED;
    discoveredState = STATE_UNDISCOVERED;
    
    //Clean up other variables
    bluetoothGatt = null;
    bluetoothAdapter = null;
    
    return true;
  }

  private boolean isNotArgsObject(JSONObject obj, CallbackContext callbackContext)
  {
  	if (obj != null)
  	{
  		return false;
  	}
  	
    JSONObject returnObj = new JSONObject();
    
    addProperty(returnObj, keyError, errorArguments);
    addProperty(returnObj, keyMessage, logNoArgObj);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean isNotService(BluetoothGattService service, CallbackContext callbackContext)
  {
  	if (service != null)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorService);
  	addProperty(returnObj, keyMessage, logNoService);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean isNotCharacteristic(BluetoothGattCharacteristic characteristic, CallbackContext callbackContext)
  {
  	if (characteristic != null)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorCharacteristic);
  	addProperty(returnObj, keyMessage, logNoCharacteristic);
    
    callbackContext.error(returnObj);
  	
  	return true;
  }
  
  private boolean isNotDescriptor(BluetoothGattDescriptor descriptor, CallbackContext callbackContext)
  {
  	if (descriptor != null)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorDescriptor);
  	addProperty(returnObj, keyMessage, logNoDescriptor);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean isNotDisconnected(CallbackContext callbackContext)
  {
    //Determine whether the device is currently connected including connecting and disconnecting
    //Certain actions like connect and reconnect can only be done while completely disconnected
    if (connectionState == BluetoothProfile.STATE_DISCONNECTED)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorIsNotDisconnected);
  	addProperty(returnObj, keyMessage, logIsNotDisconnected);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean isDisconnected(CallbackContext callbackContext)
  {
    //Determine whether the device is currently disconnected NOT including connecting and disconnecting
    //Certain actions like disconnect can be done while connected, connecting, disconnecting
  	if (connectionState != BluetoothProfile.STATE_DISCONNECTED)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorIsDisconnected);
  	addProperty(returnObj, keyMessage, logIsDisconnected);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean isNotConnected(CallbackContext callbackContext)
  {
    //Determine whether the device is currently disconnected including connecting and disconnecting
    //Certain actions like read/write operations can only be done while completely connected
  	if (connectionState == BluetoothProfile.STATE_CONNECTED)
  	{
  		return false;
  	}
  	
  	JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorIsNotConnected);
  	addProperty(returnObj, keyMessage, logIsNotConnected);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  private boolean wasNeverConnected(CallbackContext callbackContext)
  {
    //Determine whether a connection was ever attempted on the device
    if (bluetoothGatt != null)
    {
      return false;
    }
    
    JSONObject returnObj = new JSONObject();
  	
  	addProperty(returnObj, keyError, errorNeverConnected);
  	addProperty(returnObj, keyMessage, logNeverConnected);
    
    callbackContext.error(returnObj);
    
    return true;
  }
  
  //General Helpers
  private void addProperty(JSONObject obj, String key, Object value)
  {
  	//Believe exception only occurs when adding duplicate keys, so just ignore it
  	try
  	{
  		obj.put(key, value);
  	}
  	catch (JSONException e)
  	{
  		
  	}
  }
  
  private JSONObject getArgsObject(JSONArray args)
  {
    if (args.length() == 1)
    {
      try
      {
        return args.getJSONObject(0);
      }
      catch (JSONException ex)
      {
      }
    }
    
    return null;
  }
  
  private byte[] getValue(JSONObject obj)
  {
    String string = obj.optString(keyValue, null);
    
    if (string == null)
    {
      return null;
    }
    
    byte[] bytes = Base64.decode(string, Base64.NO_WRAP);
    
    if (bytes == null || bytes.length == 0)
    {
      return null;
    }
    
    return bytes;
  }
  
  private void addValue(JSONObject obj, byte[] bytes)
  {
    String string = Base64.encodeToString(bytes, Base64.NO_WRAP);
    
    addProperty(obj, keyValue, string);
  }
  
  private UUID[] getServiceUuids(JSONObject obj)
  {
    JSONArray array = obj.optJSONArray(keyServiceAssignedNumbers);
    
    if (array == null)
    {
    	return null;
    }
    
	  //Create temporary array list for building array of UUIDs
	  ArrayList<UUID> arrayList = new ArrayList<UUID>();
	  
	  //Iterate through the UUID strings
	  for (int i = 0; i < array.length(); i++)
	  {
	    String value = array.optString(i, null);
	    
	    if (value == null)
	    {
	      continue;
	    }
	    
	    String uuidString = String.format(uuidBase, value);
	       
	    //Try converting string to UUID and add to list
	    try
	    {
	      UUID uuid = UUID.fromString(uuidString);
	      arrayList.add(uuid);
	    }
	    catch (Exception ex)
	    {
	    }
	  }
	  
	  //If anything was actually added, convert list to array
	  int size = arrayList.size();
    
    if (size == 0)
    {
      return null;
    }
    
    UUID[] uuids = new UUID[size];
    uuids = arrayList.toArray(uuids);
    return uuids;
  }
  
  private String getAddress(JSONObject obj)
  {
    //Get the address string from arguments
    String address = obj.optString(keyAddress, null);
    
    if (address == null)
    {
      return null;
    }
    
    //Validate address format
    if (!BluetoothAdapter.checkBluetoothAddress(address))
    {
      return null;
    }
    
    return address;
  }
  
  private JSONObject getDiscovery()
  {
    JSONObject deviceObject = new JSONObject();
    
    BluetoothDevice device = bluetoothGatt.getDevice();
    
    addProperty(deviceObject, keyStatus, statusDiscovered);
    addProperty(deviceObject, keyAddress, device.getAddress());
    addProperty(deviceObject, keyName, device.getName());
    
    JSONArray servicesArray = new JSONArray();
    
    List<BluetoothGattService> services = bluetoothGatt.getServices();
    
    for (BluetoothGattService service : services)
    {
      JSONObject serviceObject = new JSONObject();
      
      addProperty(serviceObject, keyServiceAssignedNumber, getAssignedNumber(service.getUuid()));
      
      JSONArray characteristicsArray = new JSONArray();
      
      List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
      
      for (BluetoothGattCharacteristic characteristic : characteristics)
      {
        JSONObject characteristicObject = new JSONObject();
        
        addProperty(characteristicObject, keyCharacteristicAssignedNumber, getAssignedNumber(characteristic.getUuid()));
        
        JSONArray descriptorsArray = new JSONArray();
        
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        
        for (BluetoothGattDescriptor descriptor : descriptors)
        {
          JSONObject descriptorObject = new JSONObject();
          
          addProperty(descriptorObject, keyDescriptorAssignedNumber, getAssignedNumber(descriptor.getUuid()));
          
          descriptorsArray.put(descriptorObject); 
        }
        
        addProperty(characteristicObject, keyDescriptors, descriptorsArray);
        
        characteristicsArray.put(characteristicObject);
      }
      
      addProperty(serviceObject, keyCharacteristics, characteristicsArray);
      
      servicesArray.put(serviceObject);
    }
    
    addProperty(deviceObject, keyServices, servicesArray);
    
    return deviceObject;
  }
  
  private String getAssignedNumber(UUID input)
  {
  	String output = input.toString();
  	
  	return output.substring(4, 8);
  }
}
