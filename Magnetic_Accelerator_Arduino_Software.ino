// Lap properties 
const float LAP_LENGTH = 0.145*2*PI; // Lap length in meters
const float SENSOR_COIL_LENGTH = 0.05; // Distance between hall sensor and coil in meters
const float INLET_LENGTH = 0.135; // Distance between inlet hall sensor (sensor 2) and the next hall sensor (sensor 0)

// Other adjustable contants 
const long INERTIA = 0; // Correction for inertia in the detection in milliseconds
const int TIMEOUT = 6000; // Coils will turn off if no ball is detected within this time (in milliseconds) after the previous detection
const int TRIGGER = 100; // Trigger value for the hall sensor (a number between 0 (low threshold) and 510 (high threshold);

// Pins
const byte LED_PIN = 13;
const byte COIL_PIN_0 = 4;
const byte COIL_PIN_1 = 5;
const byte INPUT_PIN_0 = A0; 
const byte INPUT_PIN_1 = A1;
const byte INPUT_PIN_2 = A2;

// Identification constants --- This constants need to be the same as in the Java program, so don't change them unless with a good reason
const String NAME = "MIBArduino";
const String CONFIRM = "Connected!";
const byte ID = 123; // Identification Number 
const byte CN = 34;  // Control Number

// Command constants --- This constants need to be the same as in the Java program, so don't change them unless with a good reason
const byte ACC = 100;  // Accelerate
const byte DCC = 101; // De-accelerate
const byte STOP = 102; // Stop accelerating or de-accelerating

// Variables
float velocity = 0.0;
float previousVelocity = 0.0;
unsigned long oldTime = 0;
unsigned long timeNow = 0;

byte previousSensor = 55;

boolean isConnected = false;
boolean isBetween0And1 = false;
boolean isAccelerating = false;
boolean isDecelerating = false;
boolean isTimeOut = true;
boolean hasBegun = true;


void setup() {
  Serial.begin(9600);
  Serial.println(LAP_LENGTH);

  pinMode(COIL_PIN_0, OUTPUT);
  pinMode(COIL_PIN_1, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  
  //pinMode(A0, INPUT);
  //pinMode(A1, INPUT);

  digitalWrite(COIL_PIN_0, LOW);
  digitalWrite(COIL_PIN_1, LOW);
  digitalWrite(LED_PIN, HIGH);
  
  
}

void loop() {
  //delayMicroseconds(300);
  
  if(!isConnected){
    if(Serial.available() > 1){
      
      if(Serial.read() == ID){
        if(Serial.read() == CN){
                    
          isConnected = true;
          Serial.print(CONFIRM);

          while(Serial.available() > 0){
            Serial.read();
          }
          delay(50);
          sendFloat(LAP_LENGTH);
                              
        }
        else{
          Serial.print(NAME);
        }  
      }
      else{
        Serial.print(NAME);
      }
    }
    else{
      Serial.print(NAME);
    }
    delay(10);
  }
  else {
    //int m = 512;
    boolean triggered = false;
    byte sensor;
    long coilOff;
    long nextCoilOn;

    if(!hasBegun){
     
      int m2 = analogRead(INPUT_PIN_2);
      //Serial.print("meting 2 ");
        //Serial.println(m2);
      if(m2 > 512 + TRIGGER || m2 < 512 - TRIGGER){
        hasBegun = true;
        oldTime = millis();
        previousSensor = sensor;
        sensor = 2;
      }
    }
    else{
      int m0 = analogRead(INPUT_PIN_0);
      int m1 = analogRead(INPUT_PIN_1);
      //Serial.print("meting 0 ");
      //Serial.println(m0);
      //Serial.print("meting 1 ");
      //Serial.println(m1);
      if(m0 > 512 + TRIGGER || m0 < 512 - TRIGGER){
        previousSensor = sensor;
        sensor = 0;
        triggered = true;
        //Serial.println("TRUEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        //m = m0;
      }
      else if(m1 > 512 + TRIGGER || m1 < 512 - TRIGGER){
        previousSensor = sensor;
        sensor = 1;
        triggered = true;
        //Serial.println("TRUEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        //m = m1;
      }

    }
    
    if(triggered){
      triggered = false;
      
      
      timeNow = millis();
      previousVelocity = velocity;
      previousSensor = sensor;
      velocity = 500*LAP_LENGTH/(timeNow - oldTime);  
      
        //Serial.println(velocity);
        
        //sendFloat(velocity);
        
        if(isAccelerating || isDecelerating){
          
          if(isAccelerating && sensor != 2){
            if(sensor == 0){
              digitalWrite(COIL_PIN_0, HIGH);
              digitalWrite(COIL_PIN_1, LOW);
            }
            else{
              digitalWrite(COIL_PIN_0, LOW);
              digitalWrite(COIL_PIN_1, HIGH);
            }
          }
          
                       
            coilOff = 1000*SENSOR_COIL_LENGTH/(2*velocity - previousVelocity);
            //Serial.print(velocity);
              //Serial.print("\t");
              //Serial.print(oldTime);
              //Serial.print("\t");
              //Serial.println(coilOff);
              delay(80 - 20*velocity);
              

              sendFloat(velocity);
            if(coilOff > INERTIA){
              //delay(coilOff - INERTIA);       
            }
            digitalWrite(COIL_PIN_0, LOW);
            digitalWrite(COIL_PIN_1, LOW);
            
            if(isAccelerating){
              if(sensor == 0){
                
                //Serial.println("A s 0");
              }
              else if(sensor == 1){
                
                //Serial.println("A s 1");
              }              
              
            }
            else if(isDecelerating){
              
            }
            oldTime = timeNow;
            previousSensor = sensor;
            isTimeOut = false;
            delay(140);    
        } 
    }
    else if(millis() - oldTime > TIMEOUT){
      isTimeOut = true;
      if(velocity != 0.0){
        sendFloat(0.0);
      }
      velocity = 0.0;
      digitalWrite(COIL_PIN_0, LOW);
      digitalWrite(COIL_PIN_1, LOW);
    }
    
    if(Serial.available() > 0){
      byte command = Serial.read();

      switch(command){
        case ACC:
          isAccelerating = true;
          isDecelerating = false;
          break;
          
        case DCC:
          isAccelerating = false;
          isDecelerating = true;
          break;

        case STOP:
          isAccelerating = false;
          isDecelerating = false;
          digitalWrite(COIL_PIN_0, LOW);
          digitalWrite(COIL_PIN_1, LOW);
          break;  
      }
    }
    
  }
}

void sendLong(long l){
  byte * b = (byte *) & l;
  Serial.write(b[3]);
  Serial.write(b[2]);
  Serial.write(b[1]);
  Serial.write(b[0]);
}

void sendFloat(float f){
  byte * b = (byte *) & f;
  Serial.write(b[3]);
  Serial.write(b[2]);
  Serial.write(b[1]);
  Serial.write(b[0]);
}


