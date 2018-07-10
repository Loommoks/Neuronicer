package su.zencode.neuronicer;
import java.util.LinkedList;

public class NetworkStarter {

    public static double NETWORK_LEARN_ACCURACY = 0.005;
    protected static int inputsCount  = 784;
    protected static int hiddenLayerOneCount = 78;
    protected static int hiddenLayerTwoCount = 39;
    protected static int outputsCount = 10;

    public static int getInputsCount(){return inputsCount;}
    public static int getHiddenLayerOneCount(){return hiddenLayerOneCount;}
    public static int getHiddenLayerTwoCount(){return hiddenLayerTwoCount;}
    public static int getOutputsCount(){return outputsCount;}
    public static double getNetworkLearnAccuracy(){return NETWORK_LEARN_ACCURACY;}

    public static void setInputsCount(int inputsCount) {
        NetworkStarter.inputsCount = inputsCount;
        System.out.println("Установлено количество нейронов входного слоя: "+inputsCount);
    }

    public static void setNetworkLearnAccuracy(double newAccuracy){
        NetworkStarter.NETWORK_LEARN_ACCURACY = newAccuracy;
        System.out.println("Установлено новая точность обучения сети: "+NETWORK_LEARN_ACCURACY);
        //GUIPanel.writeToConsole("Установлено новая точность обучения сети: "+Double.toString(NETWORK_LEARN_ACCURACY)+"\n");
    }

    public static int[] getNeuronsInLayers(){
        int[] neuronsInLayers = {
            inputsCount,
            hiddenLayerOneCount,
            hiddenLayerTwoCount,
            outputsCount
        };
        return neuronsInLayers;
    }

    public static void Start (Network Net, double[][] inputs, double[][] outputs){

        int[] neuronsInLayers = {
                inputsCount,
                hiddenLayerOneCount,
                hiddenLayerTwoCount,
                outputsCount
        };

        Net.initializeNetwork(neuronsInLayers);

        Net.showNetworkData();
        Net.runBPA(inputs, outputs, NETWORK_LEARN_ACCURACY);
        Net.showNetworkData();
    }

    public static void Start (Network Net, LinkedList<double[]> inputs, LinkedList<double[]> outputs){

        int[] neuronsInLayers = {
                inputsCount,
                hiddenLayerOneCount,
                hiddenLayerTwoCount,
                outputsCount
        };

        Net.initializeNetwork(neuronsInLayers);

        Net.showNetworkData();
        Net.runBPA(inputs, outputs, NETWORK_LEARN_ACCURACY);
        Net.showNetworkData();
    }

    public static void Start (Network Net, LinkedList<Sample> samples, double targetPercentage, boolean initializeNewNetwork){

        if (initializeNewNetwork){
            int[] neuronsInLayers = {
                    inputsCount,
                    hiddenLayerOneCount,
                    hiddenLayerTwoCount,
                    outputsCount
            };
            Net.initializeNetwork(neuronsInLayers);
        }

        //Net.showNetworkData();
        Net.runBPA(samples, targetPercentage);
        //Net.showNetworkData();
    }
}