package su.zencode.neuronicer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AndroidNetworkLoader extends Activity {
    protected Network net;
    protected int[] neuronsInLayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        readAFile();
    }

    public void readAFile() {
        try {

            InputStream inputStream = getResources().openRawResource(R.raw.network980);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String lineToConvert;
            while ((lineToConvert = reader.readLine()) !=null){

                String[] separated = lineToConvert.split("/");

                if (lineToConvert.startsWith("NetworkDimension")) {
                    readNetworkDimension(separated);
                } else {
                    int[] location;
                    double[] inputsWeight;
                    double[] inputsValue;
                    double out;
                    double sum;
                    //---
                    double[] inputsSigma;
                    double sigmaSum;
                    double sigmaToTrasfer;
                    double[] vector;
                    double[] dW;
                    double[] lastDW;

                    location = neuronLocationConverter(separated[0]);
                    inputsWeight = stringToDoubleArrayConverter(separated[2]);
                    inputsValue = stringToDoubleArrayConverter(separated[4]);
                    out = Double.parseDouble(separated[6]);
                    sum = Double.parseDouble(separated[8]);
                    inputsSigma = stringToDoubleArrayConverter(separated[10]);
                    sigmaSum = Double.parseDouble(separated[12]);
                    sigmaToTrasfer = Double.parseDouble(separated[14]);
                    vector = stringToDoubleArrayConverter(separated[16]);
                    dW = stringToDoubleArrayConverter(separated[18]);
                    //lastDW = stringToDoubleArrayConverter(separated[20]);

                    int neuronLayer = location[0];
                    int neuronNumber = location[1];
                    net.neuronNet[neuronLayer][neuronNumber].setInputsWeight(inputsWeight);
                    net.neuronNet[neuronLayer][neuronNumber].setInputsValue(inputsValue);
                    net.neuronNet[neuronLayer][neuronNumber].setOut(out);
                    net.neuronNet[neuronLayer][neuronNumber].setSum(sum);
                    net.neuronNet[neuronLayer][neuronNumber].setInputsSigma(inputsSigma);
                    net.neuronNet[neuronLayer][neuronNumber].setSigmaSum(sigmaSum);
                    net.neuronNet[neuronLayer][neuronNumber].setSigmaToTrasfer(sigmaToTrasfer);
                    net.neuronNet[neuronLayer][neuronNumber].setEWVector(vector);
                    net.neuronNet[neuronLayer][neuronNumber].setdW(dW);
                    //todo if(neuronLayer>0){net.neuronNet[neuronLayer][neuronNumber].set}

                }

            }
            reader.close();

        } catch (Exception ex){
            ex.printStackTrace();
        }

        Toast.makeText(this, "Network just loaded", Toast.LENGTH_SHORT).show();
        Intent networkLoadedIntent = new Intent(this,MainActivity.class);
        //networkLoadedIntent.putExtra(MainActivity.NETWORK_CODE,net);

    }

    public void readNetworkDimension(String[] dataToConvert){

        int layerscount = Integer.parseInt(dataToConvert[1]);
        neuronsInLayers = new int[layerscount];

        String[] neuronsInLayersStr = dataToConvert[2].split(",");
        for (int i=0;i<neuronsInLayersStr.length;i++){
            neuronsInLayers[i] = Integer.parseInt(neuronsInLayersStr[i]);
        }
        net = new Network();
        net.initializeNetwork(neuronsInLayers);
        net.showNetworkData();
    }

    public int[] neuronLocationConverter (String locationToConvert) {
        int[] location = new int[2];
        String[] locationString =locationToConvert.split(",");
        location[0]=Integer.parseInt(locationString[0]);
        location[1]=Integer.parseInt(locationString[1]);
        return location;
    }

    public double[] stringToDoubleArrayConverter (String toConvert) {

        String[] toReturnString = toConvert.split(",");
        double[] toReturn = new double[toReturnString.length];
        for (int i=0;i<toReturnString.length;i++) {
            toReturn[i] = Double.parseDouble(toReturnString[i]);
        }
        return toReturn;
    }
}
