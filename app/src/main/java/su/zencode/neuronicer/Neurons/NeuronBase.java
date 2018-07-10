package su.zencode.neuronicer.Neurons;


public class NeuronBase {

    protected int layerLevel;
    protected int neuronNumber;
    protected double[] inputsWeight;
    protected double[] inputsValue;
    protected double out;
    private double sum;

    protected double[] inputsSigma;
    protected double sigmaSum;
    protected double sigmaToTrasfer;
    protected double[] vector;
    protected double[] dW;



    //-----------Геттеры и сеттеры-------------------//

    public int getLayerLevel(){ return layerLevel; }

    public int getNeuronNumber(){ return neuronNumber; }

    public double getInputWeight(int inputNumber){return inputsWeight[inputNumber];}

    public double[] getInputWeight(){return inputsWeight;}

    public void setInputsWeight (double[] in) {inputsWeight = in;}

    public void setInputsValue (double[] in) {inputsValue = in;}

    public void setInputsValue(double v, int num){inputsValue[num]=v;}

    public void setInputsSigma(double s, int num){inputsSigma[num]=s;}

    public void setInputsSigma(double[] in){inputsSigma=in;}

    public double[] getInputsSigma(){return inputsSigma;}

    public void setSigmaSum (double s){sigmaSum =s;}

    public double getSigmaSum (){return sigmaSum;}

    public double getSigmaToTrasfer (){return sigmaToTrasfer;}

    public void setSigmaToTrasfer(double in){sigmaToTrasfer=in;}

    public double getInputValue(int num){return inputsValue[num];}

    public double[] getInputsValue(){return inputsValue;}

    public double getOut(){
        return out;
    }

    public void setOut(double o){out = o;}

    public void setEWVector(double[] v){vector = v;}

    public double[] getVector(){return vector;}

    public double getSum(){return sum;}

    public void setSum(double in){sum = in;}

    public double[] getdW(){return dW;}

    public void setdW(double[] in){dW=in;}

    //------------------Методы--------------------------------//

    //Подсчет результата функции активации (сигмоида 1/(1+е^(-х)) )
    public void calculateActivationFunctionOut(){
        double b;
        b = 1/(1+(Math.pow(Math.E,-sum)));
        out = b;
    }

    //Подсчет взешанной суммы
    public void calculateSummator(){
        double inputSum = 0;
        for (int i =0; i<inputsWeight.length;i++){
            inputSum = inputSum + inputsWeight[i]*inputsValue[i];
        }
        sum = inputSum;
    }




    //Функция активации новый метод
    public double aF(double in){
        double a = 1/(1+(Math.pow(Math.E,-in)));
        return a;
    }

    //--3-- Считаем взвешанную сумму ошибок входящих в нейрон
    public void calcSigmaSum (){
        sigmaSum=0;
        for (int k=0;k<vector.length;k++){
            sigmaSum+= vector[k]*inputsSigma[k];
        }
    }

    //--4-- Считаем итоговую сигму для j-го нейрона
    public void calcSigmaToTransfer (){
        sigmaToTrasfer=sigmaSum*aF(sum)*(1-aF(sum));
    }


}
