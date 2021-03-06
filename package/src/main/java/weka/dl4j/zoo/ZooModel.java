package weka.dl4j.zoo;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import weka.core.Option;
import weka.core.OptionHandler;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;

public interface ZooModel extends Serializable, OptionHandler {
  /**
   * Initialize the ZooModel as MLP
   *
   * @param numLabels Number of labels to adjust the output
   * @param seed Seed
   * @return MultiLayerNetwork of the specified ZooModel
   * @throws UnsupportedOperationException Init(...) was not supported (only CustomNet)
   */
  ComputationGraph init(int numLabels, long seed, int[][] shape)
      throws UnsupportedOperationException;

  /**
   * Convert a MultiLayerConfiguration into a Computation graph
   *
   * @param mlc Layer-wise configuration
   * @param shape Inputshape
   * @return ComputationGraph based on the configuration in the MLC
   */
  default ComputationGraph mlpToCG(MultiLayerConfiguration mlc, int[][] shape) {
    ComputationGraphConfiguration.GraphBuilder builder =
        new NeuralNetConfiguration.Builder()
            .trainingWorkspaceMode(WorkspaceMode.SEPARATE)
            .inferenceWorkspaceMode(WorkspaceMode.SEPARATE)
            .graphBuilder();
    List<NeuralNetConfiguration> confs = mlc.getConfs();

    // Start with input
    String currentInput = "input";
    builder.addInputs(currentInput);

    // Iterate MLN configurations layer-wise
    for (NeuralNetConfiguration conf : confs) {
      Layer l = conf.getLayer();
      String lName = l.getLayerName();

      // Connect current layer with last layer
      builder.addLayer(lName, l, currentInput);
      currentInput = lName;
    }
    builder.setOutputs(currentInput);

    // Configure inputs
    builder.setInputTypes(InputType.convolutional(shape[0][1], shape[0][2], shape[0][0]));

    // Build
    ComputationGraphConfiguration cgc = builder.build();
    return new ComputationGraph(cgc);
  }

  /**
   * Get the input shape of this zoomodel
   *
   * @return Input shape of this zoomodel
   */
  int[][] getShape();

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public default Enumeration<Option> listOptions() {

    return Option.listOptionsForClass(this.getClass()).elements();
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public default String[] getOptions() {

    return Option.getOptions(this, this.getClass());
  }

  /**
   * Parses a given list of options.
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public default void setOptions(String[] options) throws Exception {

    Option.setOptions(options, this, this.getClass());
  }
}
