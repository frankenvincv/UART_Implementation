package bku.com.uart_implementation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.List;


/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = "UART";
    private static final String UART_DEVICE_NAME = "UART0";
    private int sizeBuffer = 32;
    PeripheralManager manager = PeripheralManager.getInstance();
    List<String> deviceList = manager.getUartDeviceList();
    private UartDevice mDevice;
    TextView mTextvew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextvew = (TextView) findViewById(R.id.text);
        if (deviceList.isEmpty()){
            Log.i(TAG,"No UART port available on this device");
        }else {
            Log.i(TAG,"List of available devices : "+deviceList);
            mTextvew.setText(deviceList.toString());

        }
        try{
            mDevice = manager.openUartDevice(UART_DEVICE_NAME);
            configureUartFrame(mDevice);
            mDevice.registerUartDeviceCallback(mUartCallback);

        }catch (IOException e){
            Log.w(TAG," ",e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice != null){
            try{
                mDevice.unregisterUartDeviceCallback(mUartCallback);
                mDevice.close();
                mDevice = null;

            }catch (IOException e){
                Log.w(TAG,"Unable to access UART device",e);
            }
        }
    }

    public void writeUartData(UartDevice uart) throws IOException{
        byte[] buffer = new byte[sizeBuffer];
        int count = uart.write(buffer,buffer.length);
        Log.d(TAG,"Wrote "+count+"bytes to peripheral.");
    }

    public void readUartBuffer(UartDevice uart) throws IOException{
        final int maxCount = 32;
        byte[] buffer = new byte[sizeBuffer];

        int count;
        while((count = uart.read(buffer,buffer.length))>0){
            String s = "READ : "+count;
            mTextvew.setText(s);
            Log.d(TAG,"Read "+count+" bytes from peripheral.");
        }
    }

    private UartDeviceCallback mUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try{
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.w(TAG,"Unable to access UART device",e);
            }
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG,uart + "Error event "+error);
        }
    };

    public  void configureUartFrame(UartDevice uart)throws IOException{
        uart.setBaudrate(115200);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }
}
