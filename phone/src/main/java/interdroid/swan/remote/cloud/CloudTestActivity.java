package interdroid.swan.remote.cloud;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import interdroid.swan.R;
import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.TriStateExpressionListener;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.Expression;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.TriState;
import interdroid.swancore.swansong.TriStateExpression;
import interdroid.swancore.swansong.ValueExpression;

/**
 * Created by Roshan Bharath Das on 05/07/16.
 */
public class CloudTestActivity extends Activity {

    private static final String TAG = "CloudTestActivity";

  //  final String MY_EXPRESSION = "cloud@profiler:value?case=0{ANY,0} > 1";

   // final String MY_EXPRESSION = "wear@heartrate:heart_rate{ANY,1000}";
  //  final String MY_EXPRESSION = "self@wear_heartrate:heart_rate{ANY,0}";


  //  final String MY_EXPRESSION = "self@wear_movement:x{ANY,0}";
  //  final String MY_EXPRESSION = "wear@movement:x{ANY,0}";

//    final String MY_EXPRESSION = "cloud@profiler:value?case=1{ANY,0}";

    final String MY_EXPRESSION = "self@cloudtest:value?delay='1000'{MEAN,1000}";


    //   final String MY_EXPRESSION = "cloud@tree:branch{ANY,1000}";
  //  final String MY_EXPRESSION = "self@profiler:value?case=0{ANY,0} > 1";

  //  final String MY_EXPRESSION = "self@light:lux{ANY,0}";

    long avgReqTime = 0;
    int reqCount = 0;
    boolean firstReq = true;
    boolean validValue;

    /* random id */
    public final String REQUEST_CODE = "cloud-test-light";

    boolean stop = false;

    TextView tv = null;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloudtest);

        addListenerOnButton();

//        new CountDownTimer(5*60000, 1000) {
//
//
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                unregisterSWANSensor();
//            }
//
//        }.start();

        initialize();




    }


    public void initialize(){

        tv = (TextView) findViewById(R.id.textView1);

        registerSWANSensor(MY_EXPRESSION);

    }


    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                unregisterSWANSensor();


            }
        });

    }




    /* Register expression to SWAN */
    private void registerSWANSensor(String myExpression){

        try {
            Expression checkExpression =  ExpressionFactory.parse(myExpression);

            if(checkExpression instanceof ValueExpression) {
                validValue = false;
                final long[] startValue = {System.currentTimeMillis()};

                ExpressionManager.registerValueExpression(this, String.valueOf(REQUEST_CODE),
                        (ValueExpression) ExpressionFactory.parse(myExpression),
                        new ValueExpressionListener() {

                            /* Registering a listener to process new values from the registered sensor*/
                            @Override
                            public void onNewValues(String id, TimestampedValue[] arg1) {
                                if (arg1 != null && arg1.length > 0) {
                                    long result = System.currentTimeMillis()- startValue[0];

                                    // we skip the first req as it usually takes much longer
                                    if(firstReq) {
                                        validValue = true;
                                    }

                                    if(validValue) {
                                        String value = arg1[0].getValue().toString();
                                        tv.setText("Value = " + value+"\nTimestamp = "+arg1[0].getTimestamp());
                                        Log.e("CloudTestActivity","req time = "+result + " / swan time = " + arg1[0].getValue());

                                        if(!firstReq) {
                                            avgReqTime += result;
                                            reqCount++;
                                        } else {
                                            firstReq = false;
                                        }

                                        ExpressionManager.unregisterExpression(CloudTestActivity.this, String.valueOf(REQUEST_CODE));
                                        if (!stop) {
                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    registerSWANSensor(MY_EXPRESSION);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        validValue = true;
                                    }
                                } else {
                                    tv.setText("Value = null");
                                }

                            }
                        });

            }
            else if(checkExpression instanceof TriStateExpression){

                final long[] startTriState = {System.currentTimeMillis()};
                ExpressionManager.registerTriStateExpression(this, String.valueOf(REQUEST_CODE),
                        (TriStateExpression) ExpressionFactory.parse(myExpression), new TriStateExpressionListener() {
                            @Override
                            public void onNewState(String id, long timestamp, TriState newState) {

                                long endTriState = System.currentTimeMillis();
                                long resultState = (endTriState- startTriState[0]);
                                startTriState[0] = endTriState;

                                Log.e("CloudTestActivity","Time taken to get tristate result(milli seconds) "+resultState);

                                    tv.setText("Tristate ="+newState+"\nTimestamp = "+timestamp);



                            }
                        });


            }




        } catch (SwanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /* Unregister expression from SWAN */
    private void unregisterSWANSensor(){

        ExpressionManager.unregisterExpression(this, String.valueOf(REQUEST_CODE));
        stop = true;
        Log.d(TAG, "avg request time = " + ((double)avgReqTime) / reqCount);
    }


    @Override
    protected void onPause() {
        super.onPause();
      //  unregisterSWANSensor();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
     //   unregisterSWANSensor();

    }







}
