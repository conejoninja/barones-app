//MQ303A Testing - A simple sketch to understand the working of Alcohol sensor. Can be used to study how to calibrate threshold values.   
 
//Alcohol Sensor DAT Pin is connected to Analog Input Pin 0 (A0)
#define analogInDatPin A1
 
//Alcohol Sensor SEL Pin is connected to Analog Input Pin 1 (A1). In this case it is used as digital ouput.
//15 is mapped to A1
#define heaterSelPin 13
 
int sensorValue = 0;        
 
void setup() {
  pinMode(heaterSelPin,OUTPUT);    // set the heaterSelPin as digital output.
  digitalWrite(heaterSelPin,HIGH); //when heaterSelPin is set, heater is switched off.
  Serial.begin(9600);  // open the serial port at 9600 bps
}
void loop() {
  digitalWrite(heaterSelPin,LOW);             //switch on the heater of Alcohol sensor
  sensorValue = analogRead(analogInDatPin);   //read the analog value 
 
  //Disply the results in serial monitor.
  Serial.print("sensor test value = ");                       
  //sensorValue goes down when alcohol is detected. Hence subtracting from 1023.
  Serial.println(1023-sensorValue); 
  delay(100);  
}
