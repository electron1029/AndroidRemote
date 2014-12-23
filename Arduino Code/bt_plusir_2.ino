#include <SoftwareSerial.h>

int bluetoothTx = 2;
int bluetoothRx = 3;
int IRledPin =  13;    // LED recieves signal from pin 13
int readyToProcess = 0;
int dataCount = 0;
//String codes_buff = "";
char codes_buff[1000];
unsigned int codes[80];
bool readyToSend = false;
int k = 0;

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  int i = 0;
  //Setup usb serial connection to computer
  pinMode(IRledPin, OUTPUT);
  Serial.begin(9600);
  delay(100);
  bluetooth.begin(9600);
  
  for(i = 0; i < 80; i++)
  {
    codes[i] = 0;
  }
}

void loop()
{
  //Read from bluetooth
  if(bluetooth.overflow())
  {
  //  Serial.println("I USED TO WORK LOL");
  }
  if(bluetooth.available())
  {
     codes_buff[k++] = (char)bluetooth.read();
     if (codes_buff[k-1] == '.')
     {
         readyToProcess = 1;
     }
  } else if(readyToProcess)
  {
    readyToProcess = 0;
    process();
  }
}

void process()
{
    String str_num = "";
    int i = 0;
    dataCount = 0;
    
    for(i = 0; i < k; i++)
    {
      char c = codes_buff[i];
      //If we reached one of these, 
      //we are finished with current num
      
      //Done
      if(c == '.')
      {
        str_num += c;
        codes[dataCount] = str_num.toInt();
        readyToSend = true;
        k = 0;
        break;
      } else if(c == ',' || c == '\n') 
      {
        codes[dataCount] = str_num.toInt();
        dataCount++;
        if (dataCount > 79)
        {
          dataCount = 0;
          k = 0;
        }
        str_num = "";
      } else //Otherwise, keep adding chars
      {
        str_num += c;
      }
    }
    
    if (readyToSend)
    {
      SendIRCode();
    }
}

// this method is to pulse the LED pin for the correct time
void pulseIR(long microsecs) 
{
  cli();  // disable interrupts
  
  // timing in very important for this loop
  while (microsecs > 0) 
  {
   digitalWrite(IRledPin, HIGH);  // pulse the IR pin high.  This takes 3 microseconds.
   delayMicroseconds(10);         // wait for 10 microseconds
   digitalWrite(IRledPin, LOW);   // pulse the IR pin low.  This takes 3 microseconds
   delayMicroseconds(10);         // wait for 10 microseconds
   microsecs -= 26;               // decrement the time by however many microseconds have passed: 3+10+3+10=26
  }
  
  sei();  // enable interrupts
}

void SendIRCode() 
{
  int i = 0;

  //timing is important so explicitly make calls instead of adding delays
  pulseIR(codes[0]);
  delayMicroseconds(codes[1]);
  pulseIR(codes[2]);
  delayMicroseconds(codes[3]);
  pulseIR(codes[4]);
  delayMicroseconds(codes[5]);
  pulseIR(codes[6]);
  delayMicroseconds(codes[7]);
  pulseIR(codes[8]);
  delayMicroseconds(codes[9]);
  pulseIR(codes[10]);
  delayMicroseconds(codes[11]);
  pulseIR(codes[12]);
  delayMicroseconds(codes[13]);
  pulseIR(codes[14]);
  delayMicroseconds(codes[15]);
  pulseIR(codes[16]);
  delayMicroseconds(codes[17]);
  pulseIR(codes[18]);
  delayMicroseconds(codes[19]);
  pulseIR(codes[20]);
  delayMicroseconds(codes[21]);
  pulseIR(codes[22]);
  delayMicroseconds(codes[23]);
  pulseIR(codes[24]);
  delayMicroseconds(codes[25]);
  pulseIR(codes[26]);
  delayMicroseconds(codes[27]);
  pulseIR(codes[28]);
  delayMicroseconds(codes[29]);
  pulseIR(codes[30]);
  delayMicroseconds(codes[31]);
  pulseIR(codes[32]);
  delayMicroseconds(codes[33]);
  pulseIR(codes[34]);
  delayMicroseconds(codes[35]);
  pulseIR(codes[36]);
  delayMicroseconds(codes[37]);
  pulseIR(codes[38]);
  delayMicroseconds(codes[39]);
  pulseIR(codes[40]);
  delayMicroseconds(codes[41]);
  pulseIR(codes[42]);
  delayMicroseconds(codes[43]);
  pulseIR(codes[44]);
  delayMicroseconds(codes[45]);
  pulseIR(codes[46]);
  delayMicroseconds(codes[47]);
  pulseIR(codes[48]);
  delayMicroseconds(codes[49]);
  pulseIR(codes[50]);
  delayMicroseconds(codes[51]);
  pulseIR(codes[52]);
  delayMicroseconds(codes[53]);
  pulseIR(codes[54]);
  delayMicroseconds(codes[55]);
  pulseIR(codes[56]);
  delayMicroseconds(codes[57]);
  pulseIR(codes[58]);
  delayMicroseconds(codes[59]);
  pulseIR(codes[60]);
  delayMicroseconds(codes[61]);
  pulseIR(codes[62]);
  delayMicroseconds(codes[63]);
  pulseIR(codes[64]);
  delayMicroseconds(codes[65]);
  pulseIR(codes[66]);
  delayMicroseconds(codes[67]);
  pulseIR(codes[68]);
  delayMicroseconds(codes[69]);
  pulseIR(codes[70]);
  delayMicroseconds(codes[71]);
  pulseIR(codes[72]);
  delayMicroseconds(codes[73]);
  pulseIR(codes[74]);
  delayMicroseconds(codes[75]);
  pulseIR(codes[76]);
  delayMicroseconds(codes[77]);
  pulseIR(codes[78]);
  delayMicroseconds(codes[79]);
  
  // reset array after
  for (i = 0; i < 80; i++)
   {
      codes[i] = 0;
   }
 
   readyToSend = false; 
}
