package strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class NeuralNetWorkDL4J {

	Random rng;
	
	MultiLayerNetwork net;
	int numInputs;
	int numOutputs;
	
	
	public NeuralNetWorkDL4J(double learningRate, int seed, int numInputs, int numOutputs ){
		
		rng = new Random(seed);
		
		this.numInputs = numInputs;
		this.numOutputs = numOutputs;

		
        int nHidden = 20;
        
        
        net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                //.updater(new Sgd(learningRate))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(nHidden)
                        .activation(Activation.SIGMOID)
                        .build())

                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(nHidden).nOut(numOutputs).build())
                .build()
        );
        

        
        net.init();
        net.setListeners(new ScoreIterationListener(1));
        

	}
	
	public void fit(ArrayList<TrainExample> trainExamples, int nEpochs, int batchSize)
	{
		
		
		double[][] input = new double[trainExamples.size()][this.numOutputs];	
		double[][] output = new double[trainExamples.size()][trainExamples.get(0).getX().length];
		
				
		for(int i = 0; i < trainExamples.size(); i++ ) {
			
			input[i] = trainExamples.get(i).getX();
			output[i] = trainExamples.get(i).getY();
		}
		
		INDArray inputNDArray = Nd4j.create(input);
		INDArray outPut = Nd4j.create(output);
		
		
		DataSet dataSet = new DataSet(inputNDArray, outPut);
        List<DataSet> listDs = dataSet.asList();
        Collections.shuffle(listDs,rng);
        
        DataSetIterator iterator = new ListDataSetIterator<>(listDs,batchSize);
        
        
        for( int i=0; i<nEpochs; i++ ){
            iterator.reset();
            net.fit(iterator);
        }
        
        
	}
	
	public double[] predict(double[] features) {
		
		INDArray input = Nd4j.create(features,1,this.numInputs);
	    INDArray out = net.output(input, false);
	       
	    return  out.toDoubleVector();
	    
	}
	
}
