package su.zencode.neuronicer;

public class Sample {
    double[] sampleInI;
    double[] sampleOutI;

    public double[] getSampleIn (){return sampleInI;}

    public double[] getSampleOut (){return sampleOutI;}

    public void setSampleIn(double[] input){
        sampleInI=input;
    }

    public void setSampleInI(double input, int inputIndex){
        sampleInI[inputIndex]=input;
    }

    public double getSampleInI(int inputIndex){
        return sampleInI[inputIndex];
    }

    public void setSampleOut(double[] out){
        sampleOutI=out;
    }

    public void setSampleOutI(double out, int outIndex){
        sampleOutI[outIndex]=out;
    }

    public double getSampleOutI(int outIndex){
        return sampleOutI[outIndex];
    }

    public void initialiseSample(int inputDimension, int outputDimension){
        this.sampleInI = new double[inputDimension];
        this.sampleOutI = new double[outputDimension];
    }

    public int getInputDimension (){
        return sampleInI.length;
    }

    public int getOutputDimension (){
        return sampleOutI.length;
    }
}
