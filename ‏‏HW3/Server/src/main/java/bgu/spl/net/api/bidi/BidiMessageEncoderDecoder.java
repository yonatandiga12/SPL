package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BidiMessageEncoderDecoder implements MessageEncoderDecoder<String> {


    //private byte[] bytes = new byte[1 << 10]; //start with 1k
    private byte[] bytes = new byte[40];
    private int len = 0;
    private short foundOpCode = 0;
    private short opCode = 0;
    private byte[] opcodeBytes = new byte[2];


    @Override
    public String decodeNextByte(byte nextByte) {
        if(foundOpCode == 0) {
            opcodeBytes[0] = nextByte;
            foundOpCode++;
        }
        else if(foundOpCode == 1){
            opcodeBytes[1] = nextByte;
            foundOpCode++;
            opCode = bytesToShort(opcodeBytes);
        }
        else{
            if (nextByte == ';') {
                return popString();
            }
            pushByte(nextByte);
        }
        return null; //not a line yet
    }

    @Override
    public byte[] encode(String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String opCode = message.substring(0, 2);
        message = message.substring(2);
        byte[] opBytes = shortToBytes( Short.parseShort(opCode));
        if(opCode.equals("09")){              //Notification msg
            outputStream = composeNotification(opBytes, message);
        }
        else{                        //Ack or Error
            //String msgOpCode = message.substring(0, 2);
            if(opCode.equals("10")){         //Ack
                outputStream = composeAckMsg(opBytes, message);
            }
            else if(opCode.equals("11")){    //Error
                outputStream = composeErrormsg(opBytes, message );
            }
        }

        try {
            outputStream.write(";".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray(); //uses utf8 by default
    }

    private ByteArrayOutputStream composeAckMsg(byte[] opBytes, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        short msgOpShort = Short.parseShort(message.substring(0,2));
        try {
            outputStream.write(opBytes);
            if(msgOpShort == 7 | msgOpShort == 8){    //Stat logStat
                String currOp = message.substring(0, message.indexOf(","));
                outputStream.write(currOp.getBytes());
                message = message.substring(message.indexOf(",") + 1);
                while (message.contains(",")) {
                    String curr = message.substring(0, message.indexOf(","));
                    outputStream.write(shortToBytes(Short.parseShort(curr)));
                    message = message.substring(message.indexOf(",") + 1);
                }
            }
            else {
                while (message.contains(",")) {
                    String curr = message.substring(0, message.indexOf(","));
                    outputStream.write(curr.getBytes());
                    outputStream.write(0);
                    message = message.substring(message.indexOf(",") + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;

    }

    private ByteArrayOutputStream composeNotification(byte[] opBytes, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message = message.substring(1);   // deleting the ","
        String notifType = message.substring(0, 1);
        message = message.substring(2);
        String userName = message.substring(0, message.indexOf(","));
        message = message.substring(message.indexOf(",") + 1);
        String content = message;
        byte[] notifyTypeBytes = notifType.getBytes();
        byte[] userNameBytes = userName.getBytes();
        byte[] contentBytes = content.getBytes();
        try {
            outputStream.write(opBytes);
            outputStream.write(notifyTypeBytes);
            //outputStream.write(0);
            outputStream.write(userNameBytes);
            outputStream.write(0);
            outputStream.write(contentBytes);
            outputStream.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    private ByteArrayOutputStream composeErrormsg(byte[] opBytes, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] stringBytes = message.getBytes();
        try {
            outputStream.write(opBytes);
            outputStream.write(stringBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {
        //this is not actually required as it is the default encoding in java.
        //The dot should be the barrier between the opcode and the real message
        String opCodeString = String.valueOf(opCode);
        if(opCode < 10){
            opCodeString = "0" + opCodeString;
        }

        String result = opCodeString + new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        opCode = 0;
        foundOpCode = 0;  //for the next decode it will be 0
        opcodeBytes[0] = 0;
        opcodeBytes[1] = 0;
        return result;
    }


    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}
