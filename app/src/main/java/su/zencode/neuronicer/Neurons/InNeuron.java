package su.zencode.neuronicer.Neurons;

public class InNeuron extends NeuronBase {
    public InNeuron(int neuronNumber){
        this.neuronNumber = neuronNumber;
        this.layerLevel = 0;
        inputsWeight = new double[1];
        inputsWeight[0] = 1;
        inputsValue = new double[1];
        out = 0;
        inputsSigma = new double[1];
        sigmaSum=0;
        sigmaToTrasfer=0;
        vector = new double[1];
        dW = new double[1];
    }
    public void calculateActivationFunctionOut(){
        setOut(getSum());
    }
}
