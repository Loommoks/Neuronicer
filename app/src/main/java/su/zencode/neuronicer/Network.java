package su.zencode.neuronicer;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

import su.zencode.neuronicer.Neurons.HiddenNeuron;
import su.zencode.neuronicer.Neurons.InNeuron;
import su.zencode.neuronicer.Neurons.NeuronBase;


public class Network implements Serializable{
    NeuronBase[][] neuronNet;
    double[][] sigmaTemp;
    double[][] sigmaFull;
    double networkError;
    int learningIterations=0;

    public double getNetworkError(){
        return networkError;
    }
    public int getLearningIterations(){return learningIterations;}

    public int[] getNetworkDimension(){
        int[] neuronsInLayers = new int[neuronNet.length];
        for (int k=0;k<neuronNet.length;k++){
            neuronsInLayers[k]=neuronNet[k].length;
        }
        return neuronsInLayers;
    }

    public void initializeNetwork (int[] neuronsInLevel){
        neuronNet = new NeuronBase[neuronsInLevel.length][];

        //Создаем входной слой
        int inputsCount = neuronsInLevel[0];
        neuronNet[0] = new NeuronBase[inputsCount];
        for (int j=0; j<inputsCount;j++)
            neuronNet[0][j] = new InNeuron(j);

        //Создаем остальные слои
        sigmaTemp = new double[neuronsInLevel.length-1][];
        sigmaFull = new double[neuronsInLevel.length-1][];

        for (int level =1; level<neuronNet.length; level++) {
            neuronNet[level] = new NeuronBase[neuronsInLevel[level]];
            sigmaTemp[level - 1] = new double[neuronsInLevel[level]];
            sigmaFull[level - 1] = new double[neuronsInLevel[level]];
            int previousLevelLength = neuronNet[level - 1].length;
            for (int j=0; j<neuronNet[level].length;j++)
                neuronNet[level][j] = new HiddenNeuron(level,j, previousLevelLength,neuronsInLevel.length);
        }
        initializeDarkSide();
    }

    public void initializeDarkSide(){
        for (int i=1;i<neuronNet.length;i++){
            if(i<=neuronNet.length-2) {
                for (int j = 0; j < neuronNet[i].length; j++) {
                    ((HiddenNeuron)neuronNet[i][j])
                            .initializeNeuronDarkSide(neuronNet[i+1].length);
                }
            }else {
                if (i==neuronNet.length-1){
                    for (int j = 0; j < neuronNet[i].length; j++) {
                        ((HiddenNeuron)neuronNet[i][j])
                                .initializeNeuronOutDarkSide();
                    }
                }
            }
        }
    }

    public void showNetworkData(){
        System.out.println("Входные данные:["+neuronNet[0][0].getInputValue(0)+"]["+neuronNet[0][1].getInputValue(0)+"]");
        for (int il =0; il<neuronNet.length; il++){

            for (int jninl=0; jninl<neuronNet[il].length;jninl++){
                System.out.print("Нейрон[" +neuronNet[il][jninl].getLayerLevel() +"][" +neuronNet[il][jninl].getNeuronNumber() +"]  ");
                System.out.print("Выход:" +neuronNet[il][jninl].getOut()+"  ");
                double[] wMassive= neuronNet[il][jninl].getInputWeight();
                for (int k=0;k<wMassive.length;k++){
                    System.out.print("Вес["+k+"]: " +wMassive[k]+"  ");
                }
                System.out.println("");
            }
        }
    }

    public void showLastLayerOutputs(){

    }


    public double startNetworking(double in[]){
        //По логике даем сигнал на входные нейроны
        //Далее в цикле проходим по всем нейронам каждого слоя и они передают значения на выход
        //System.out.println("Входные данные:["+in[0]+"]["+in[1]+"]");
        for (int i=0; i<neuronNet.length;i++){
            if (i==0) {
                for (int j = 0; j < neuronNet[i].length; j++) {
                    neuronNet[i][j].setInputsValue(in[j],0);
                    neuronNet[i][j].calculateSummator();
                    neuronNet[i][j].calculateActivationFunctionOut();
                    transferSingnal(i,j);
                }
            }
            else {
                for (int j = 0; j < neuronNet[i].length; j++) {
                    neuronNet[i][j].calculateSummator();
                    neuronNet[i][j].calculateActivationFunctionOut();
                    transferSingnal(i,j);
                }
            }
        }

        return 0;

    }

    public boolean startNetworkingWithTestSample(double in[],double answer[]){
        startNetworking(in);
        int lastLayerNumber = neuronNet.length-1;
        int neuronsInLastLayer = neuronNet[lastLayerNumber].length;
        double[] result = new double[neuronsInLastLayer];
        int counter=0;
        //int answerIndex=100;
        boolean answerIsRight = false;
        int rightAnswer=-1;
        int index=0;
        for (int j=0; j<neuronsInLastLayer;j++){
            result[j]=neuronNet[lastLayerNumber][j].getOut();
            if(result[j]>0.70){
                counter++;
                index=j;
            }
            //System.out.println("Out ["+j+"]: "+result[j]);
        }

        for (int a=0; a<answer.length;a++){
            if(answer[a]>0){
                rightAnswer=a;
            }
        }
        //System.out.print("Правильный ответ: "+rightAnswer+" ");
        if(counter==1){
            //System.out.print("Думаю в семпле число: "+index+" ");
            if (index==rightAnswer){
                //System.out.println("Ура! Угадал  ");
                answerIsRight=true;
            }
        }else {//System.out.println("Что-то пошло не так, не могу определится с ответом");
             }

        return answerIsRight;

    }

    public boolean startNetworkingWithTestSampleAllIn(double in[],double answer[]){
        startNetworking(in);
        int lastLayerNumber = neuronNet.length-1;
        int neuronsInLastLayer = neuronNet[lastLayerNumber].length;
        double[] result = new double[neuronsInLastLayer];
        int counter=0;
        //int answerIndex=100;
        boolean answerIsRight = false;
        int rightAnswer=-1;
        double maxPossibilityAnswer=0;
        int index=0;
        for (int j=0; j<neuronsInLastLayer;j++){
            result[j]=neuronNet[lastLayerNumber][j].getOut();
            if(result[j]>maxPossibilityAnswer){
                maxPossibilityAnswer=result[j];
                index=j;
            }
            //System.out.println("Out ["+j+"]: "+result[j]);
        }

        for (int a=0; a<answer.length;a++){
            if(answer[a]>0){
                rightAnswer=a;
            }
        }
        //System.out.print("Правильный ответ: "+rightAnswer+" ");

            //System.out.print("Думаю в семпле число: "+index+" ");
            if (index==rightAnswer){
                //System.out.println("Ура! Угадал  ");
                answerIsRight=true;
            }


        return answerIsRight;

    }

    public int startAndroidNetworking(double in[]){
        startNetworking(in);
        int lastLayerNumber = neuronNet.length-1;
        int neuronsInLastLayer = neuronNet[lastLayerNumber].length;
        double[] result = new double[neuronsInLastLayer];
        int counter=0;
        //int answerIndex=100;
        boolean answerIsRight = false;
        int rightAnswer=-1;
        double maxPossibilityAnswer=0;
        int index=0;
        for (int j=0; j<neuronsInLastLayer;j++){
            result[j]=neuronNet[lastLayerNumber][j].getOut();
            System.out.println("вероятность, что ответ ["+j+"]: "+result[j]);
            if(result[j]>maxPossibilityAnswer){
                maxPossibilityAnswer=result[j];
                index=j;
            }
            //System.out.println("Out ["+j+"]: "+result[j]);
        }
        return index;
    }

    public void transferSingnal(int x, int y){
        if (x>=neuronNet.length){System.out.print("Network layer exceeded");}
        else {
            if (x == (neuronNet.length-1)){/*System.out.println("Выход нейрона["+x+"]["+y+"]: "+neuronNet[x][y].getOut());*/}
            else {
                for (int i = 0; i < neuronNet[x+1].length; i++) {
                    neuronNet[x+1][i].setInputsValue(neuronNet[x][y].getOut(),y);
                }
            }
        }

    }

    //--1-- Считаем вектор весов для нейрона j
    public double[] calcEWVector (int i, int j){
        if (i>neuronNet.length-2){
            return null;
        }else {
            double[] v = new double[neuronNet[i + 1].length];
            for (int k = 0; k < v.length; k++) {
                v[k] = neuronNet[i + 1][k].getInputWeight(j);
            }
            return v;
        }
    }

    //--1.1-- Считаем вектор весов для всех нейронов i-го слоя
    public void calcEWVectorForLayerI (int i){
        for (int j=0;j<neuronNet[i].length;j++){
            neuronNet[i][j].setEWVector(calcEWVector(i,j));
        }
    }

    //--3.1-- Считаем взвешанную сумму ошибок для всех нейронов i-го слоя
    public void calcSigmaSumForLayerI (int i){
        if(i>neuronNet.length-2) {

        }else {
            for (int j = 0; j < neuronNet[i].length; j++) {
                neuronNet[i][j].calcSigmaSum();
            }
        }
    }

    //--3.2-- Счтьаем взвешанную сумму ошибок для всех нейронов выходного слоя
    public void calcSigmaSumForOutputLayer(double[] answer){
        int i =neuronNet.length-1;
        for(int j=0; j<neuronNet[i].length;j++){
            neuronNet[i][j].setSigmaSum(answer[j]-neuronNet[i][j].getOut());
        }
    }

    //--4.1-- Считаем итоговую сигму для всех нейронов i-го слоя
    public void calcSigmaToTransferForLayerI(int i){
        for (int j=0;j<neuronNet[i].length;j++){
            neuronNet[i][j].calcSigmaToTransfer();
        }
    }

    //--5.1-- Считаем изменение весов для всех нейронов i-го слоя
    public void calcDWForLayerI(int i){

        for (int j=0;j<neuronNet[i].length;j++){
            ((HiddenNeuron)(neuronNet[i][j])).calcDW();
        }
    }

    //--5.2-- Применяем изменения весов для всей сети (кроме 1го слоя)
    public void applyDW(){
        for (int i=1;i<neuronNet.length;i++){
            for(int j=0;j<neuronNet[i].length;j++){
                ((HiddenNeuron)neuronNet[i][j]).applyDW();
            }
        }
    }

    //--6-- Передаем сигмы j-го нейрона i-го слоя на (i-1) слой
    public void transferSigmas(int i, int j){
        for (int k=0;k<neuronNet[i-1].length;k++){
            neuronNet[i-1][k].setInputsSigma(neuronNet[i][j].getSigmaToTrasfer(),j);
        }
    }

    //--6.1-- Передаем сигмы каждого нейрона i-го слоя на (i-1) слой
    public void transferSigmasFromILayer (int i){
        if (i<=1){

        }else {
            for (int j = 0; j < neuronNet[i].length; j++) {
                transferSigmas(i, j);
            }
        }
    }

    //--7-- Выполняем Backpropagation
    public void RunBackpropagation (double[] input, double[] cOutput){
        startNetworking(input);
        calcSigmaSumForOutputLayer(cOutput);
        calcSigmaToTransferForLayerI(neuronNet.length-1);
        transferSigmasFromILayer(neuronNet.length-1);
        calcDWForLayerI(neuronNet.length-1);
        for (int i=neuronNet.length-2;i>0;i--){
            calcEWVectorForLayerI(i);
            calcSigmaSumForLayerI(i);
            calcSigmaToTransferForLayerI(i);
            transferSigmasFromILayer(i);
            calcDWForLayerI(i);
        }
        applyDW();
    }

    //--8-- Выполняем Backpropagation для массива сэмплов до достижения желаемой величины ошибки
    public int runBPA(double[][] inM, double[][] outM, double accuracy){
        networkError =2;
        double networkErrorTemp=2;
        int counter=0;
        while (networkError >accuracy){networkErrorTemp=0;
        learningIterations++;
            for (int i=0;i<inM.length;i++) {
                counter++;
                RunBackpropagation(inM[i],outM[i]);
                startNetworking(inM[i]);
                for (int k = 0; k < neuronNet[neuronNet.length - 1].length; k++) {
                    networkErrorTemp += Math.pow(outM[i][k] - neuronNet[neuronNet.length - 1][k].getOut(), 2);
                }
                //System.out.println("Итерация: "+counter+" Вход ["+neuronNet[0][0].getOut()+"]["+neuronNet[0][1].getOut()+"], Выход[2][0]: "+neuronNet[2][0].getOut()+" ,Ошибка: "+ networkError);
                System.out.println("Итерация: "+learningIterations+" ,Ошибка: "+ networkError);

            }
            networkError =networkErrorTemp;

            //super.setChanged();
        }
        return counter;
    }

    public int runBPA(LinkedList<double[]> inM, LinkedList<double[]> outM, double accuracy){
        networkError =2;
        double networkErrorTemp=2;
        int counter=0;
        int counter2=0;
        int rightanswerscounter=0;
        double percentage;
        while (networkError >accuracy){
            networkErrorTemp=0;
            learningIterations++;
            counter=0;
            rightanswerscounter=0;
            LinkedList<double[]> outMTemp = outM;
            LinkedList<double[]> inMTemp = inM;
            for (int i=0;i<inM.size();i++) {
                counter++;

                if(counter==50){
                    counter=0;
                    //counter2++;
                    //System.out.print("Процент выполнения эпохи: "+counter2+"%");
                    //System.out.println("   Эпоха: "+learningIterations+"  BPA Sample: "+i+",  Ошибка: "+ networkError);
                }
                for (int t=0;t<10;t++) {
                    RunBackpropagation(inM.get(i), outM.get(i));
                }

                RunBackpropagation(inM.get(i),outM.get(i));
                //RunBackpropagation(inM.get(i),outM.get(i));
                //RunBackpropagation(inM.get(i),outM.get(i));
                //RunBackpropagation(inM.get(i),outM.get(i));
                //startNetworking(inM.get(i));
                for (int k = 0; k < neuronNet[neuronNet.length - 1].length; k++) {
                    networkErrorTemp += Math.pow(outM.get(i)[k] - neuronNet[neuronNet.length - 1][k].getOut(), 2);
                }
                //System.out.println("Итерация: "+counter+" Вход ["+neuronNet[0][0].getOut()+"]["+neuronNet[0][1].getOut()+"], Выход[2][0]: "+neuronNet[2][0].getOut()+" ,Ошибка: "+ networkError);
                //System.out.println("Итерация: "+learningIterations+"BPA Sample: "+i+" ,Ошибка: "+ networkError);

            }
            for (int i=0;i<inM.size();i++){
                if (startNetworkingWithTestSample(inM.get(i),outM.get(i))){rightanswerscounter++;}
            }
            percentage = ((double)rightanswerscounter/300);
            System.out.println("   Эпоха: "+learningIterations+",  Ошибка: "+ networkError+"  Правильных ответов: "+percentage*100+"%");
            networkError =networkErrorTemp;

            //super.setChanged();
        }
        return counter;
    }

    public int runBPA(LinkedList<Sample> samples, double targetPercentage){
        networkError =2;
        double networkErrorTemp=2;
        int counter=0;
        int counter2=0;
        int rightanswerscounter=0;
        double percentage=0;
        while (percentage <targetPercentage){
            networkErrorTemp=0;
            learningIterations++;
            counter=0;
            rightanswerscounter=0;
            Collections.shuffle(samples);
            for (int i=0;i<samples.size();i++) {
                counter++;

                /*if(counter==50){
                    counter=0;
                    //counter2++;
                    //System.out.print("Процент выполнения эпохи: "+counter2+"%");
                    //System.out.println("   Эпоха: "+learningIterations+"  BPA Sample: "+i+",  Ошибка: "+ networkError);
                }*/
                for (int t=0;t<1;t++) {
                    RunBackpropagation(samples.get(i).getSampleIn(), samples.get(i).getSampleOut());
                }

                //RunBackpropagation(samples.get(i).getSampleIn(), samples.get(i).getSampleOut());

                for (int k = 0; k < neuronNet[neuronNet.length - 1].length; k++) {
                    networkErrorTemp += Math.pow(samples.get(i).getSampleOutI(k) - neuronNet[neuronNet.length - 1][k].getOut(), 2);
                }
                //System.out.println("Итерация: "+counter+" Вход ["+neuronNet[0][0].getOut()+"]["+neuronNet[0][1].getOut()+"], Выход[2][0]: "+neuronNet[2][0].getOut()+" ,Ошибка: "+ networkError);
                //System.out.println("Итерация: "+learningIterations+"BPA Sample: "+i+" ,Ошибка: "+ networkError);

            }
            for (int i=0;i<samples.size();i++){
                if (startNetworkingWithTestSampleAllIn(samples.get(i).getSampleIn(),samples.get(i).getSampleOut())){rightanswerscounter++;}
            }
            networkError =networkErrorTemp;
            percentage = ((double)rightanswerscounter/samples.size());
            System.out.println("   Эпоха: "+learningIterations
                    +",  Ошибка: "+ networkError
                    +"  Правильных ответов: "+percentage*100+"%");


            if (percentage>0.97){break;}

            //super.setChanged();
        }
        return counter;
    }

    /*public class GUIObserver extends Observable{
        public double error;
        public void action (double e){
            error=e;
            this.notifyObservers();
        }
    }*/
}
