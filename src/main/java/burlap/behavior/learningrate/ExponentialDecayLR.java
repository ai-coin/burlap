package burlap.behavior.learningrate;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a learning rate that decays exponentially with time according to r^t, where r is in [0,1] and t is the time step,
 * from an initial learning rate. A minimum learning rate value can be specified so that the learning rate is never rounded to zero. By
 * default, the learning rate may decrease to Double.MIN_NORMAL, which is the smallest fraction a double value can hold.
 *
 * This class may be specified to use a universal learning rate that is shared regardless of state and action, or it can be set to have a
 * different learning rate for each state (or state feature) that is decayed independently of other states, or it may also be specified to
 * have a learning rate that is independently decayed for each state-action pair (or state feature-action pair). However, the state-action
 * decay will ignore any parameterizations of actions.
 *
 * @author James MacGlashan
 *
 */
public class ExponentialDecayLR implements LearningRate {

  /**
   * The initial learning rate value
   */
  protected double initialLearningRate;

  /**
   * The exponential base by which the learning rate is decayed
   */
  protected double decayRate;

  /**
   * The minimum learning rate
   */
  protected double minimumLR = Double.MIN_NORMAL;

  /**
   * The state independent learning rate
   */
  protected double universalLR;

  /**
   * The state dependent or state-action dependent learning rates
   */
  protected Map<HashableState, StateWiseLearningRate> stateWiseMap;

  /**
   * The state feature dependent or state feature-action dependent learning rates
   */
  protected Map<Integer, StateWiseLearningRate> featureWiseMap;

  /**
   * Whether the learning rate is dependent on the state
   */
  protected boolean useStateWise = false;

  /**
   * Whether the learning rate is dependent on state-actions
   */
  protected boolean useStateActionWise = false;

  /**
   * How to hash and perform equality checks of states
   */
  protected HashableStateFactory hashingFactory;

  /**
   * The last agent time at which they polled the learning rate
   */
  protected int lastPollTime = -1;

  /**
   * Initializes with an initial learning rate and decay rate for a state independent learning rate. Minimum learning rate that can be
   * returned will be Double.MIN_NORMAL
   *
   * @param initialLearningRate the initial learning rate
   * @param decayRate the exponential base by which the learning rate is decayed
   */
  public ExponentialDecayLR(double initialLearningRate, double decayRate) {
    if (decayRate > 1 || decayRate < 0) {
      throw new RuntimeException("Decay rate must be <= 1 and >= 0");
    }
    this.initialLearningRate = initialLearningRate;
    this.decayRate = decayRate;
    this.universalLR = this.initialLearningRate;
  }

  /**
   * Initializes with an initial learning rate and decay rate for a state independent learning rate that will decay to a value no smaller
   * than minimumLearningRate
   *
   * @param initialLearningRate the initial learning rate
   * @param decayRate the exponential base by which the learning rate is decayed
   * @param minimumLearningRate the smallest value to which the learning rate will decay
   */
  public ExponentialDecayLR(double initialLearningRate, double decayRate, double minimumLearningRate) {
    if (decayRate > 1 || decayRate < 0) {
      throw new RuntimeException("Decay rate must be <= 1 and >= 0");
    }
    this.initialLearningRate = initialLearningRate;
    this.decayRate = decayRate;
    this.universalLR = this.initialLearningRate;
    this.minimumLR = minimumLearningRate;
  }

  /**
   * Initializes with an initial learning rate and decay rate for a state or state-action (or state feature-action) dependent learning rate.
   * Minimum learning rate that can be returned will be Double.MIN_NORMAL. If this learning rate function is to be used for state state
   * features, rather than states, then the hashing factory can be null;
   *
   * @param initialLearningRate the initial learning rate for each state or state-action
   * @param decayRate the exponential base by which the learning rate is decayed
   * @param hashingFactory how to hash and compare states
   * @param useSeparateLRPerStateAction whether to have an independent learning rate for each state-action pair, rather than just each state
   */
  public ExponentialDecayLR(double initialLearningRate, double decayRate, HashableStateFactory hashingFactory, boolean useSeparateLRPerStateAction) {
    if (decayRate > 1 || decayRate < 0) {
      throw new RuntimeException("Decay rate must be <= 1 and >= 0");
    }
    this.initialLearningRate = initialLearningRate;
    this.decayRate = decayRate;

    this.useStateWise = true;
    this.useStateActionWise = useSeparateLRPerStateAction;
    this.hashingFactory = hashingFactory;
    this.stateWiseMap = new HashMap<HashableState, ExponentialDecayLR.StateWiseLearningRate>();
    this.featureWiseMap = new HashMap<Integer, ExponentialDecayLR.StateWiseLearningRate>();

  }

  /**
   * Initializes with an initial learning rate and decay rate for a state or state-action (or state feature-action) dependent learning rate
   * that will decay to a value no smaller than minimumLearningRate If this learning rate function is to be used for state state features,
   * rather than states, then the hashing factory can be null;
   *
   * @param initialLearningRate the initial learning rate for each state or state-action
   * @param decayRate the exponential base by which the learning rate is decayed
   * @param minimumLearningRate the smallest value to which the learning rate will decay
   * @param hashingFactory how to hash and compare states
   * @param useSeparateLRPerStateAction whether to have an independent learning rate for each state-action pair, rather than just each state
   */
  public ExponentialDecayLR(double initialLearningRate, double decayRate, double minimumLearningRate, HashableStateFactory hashingFactory, boolean useSeparateLRPerStateAction) {
    if (decayRate > 1 || decayRate < 0) {
      throw new RuntimeException("Decay rate must be <= 1 and >= 0");
    }
    this.initialLearningRate = initialLearningRate;
    this.decayRate = decayRate;
    this.minimumLR = minimumLearningRate;

    this.useStateWise = true;
    this.useStateActionWise = useSeparateLRPerStateAction;
    this.hashingFactory = hashingFactory;
    this.stateWiseMap = new HashMap<HashableState, ExponentialDecayLR.StateWiseLearningRate>();
    this.featureWiseMap = new HashMap<Integer, ExponentialDecayLR.StateWiseLearningRate>();

  }

  @Override
  public double peekAtLearningRate(State s, Action ga) {

    if (!useStateWise) {
      return this.universalLR;
    }

    StateWiseLearningRate slr = this.getStateWiseLearningRate(s);
    if (!useStateActionWise) {
      return slr.learningRate;
    }

    return slr.getActionLearningRateEntry(ga).md;
  }

  @Override
  public double pollLearningRate(int agentTime, State s, Action ga) {

    if (!useStateWise) {
      double oldVal = this.universalLR;
      if (agentTime > this.lastPollTime) {
        this.universalLR = this.nextLRVal(oldVal);
        this.lastPollTime = agentTime;
      }

      return oldVal;
    }

    StateWiseLearningRate slr = this.getStateWiseLearningRate(s);
    if (!useStateActionWise) {

      double oldVal = slr.learningRate;
      if (agentTime > slr.lastPollTime) {
        slr.learningRate = this.nextLRVal(oldVal);
        slr.lastPollTime = agentTime;
      }

      return oldVal;
    }

    MutableDouble md = slr.getActionLearningRateEntry(ga);
    double oldVal = md.md;
    if (agentTime > md.lastPollTime) {
      md.md = this.nextLRVal(oldVal);
      md.lastPollTime = agentTime;
    }

    return oldVal;

  }

  @Override
  public double peekAtLearningRate(int featureId) {

    if (!useStateWise) {
      return this.universalLR;
    }

    StateWiseLearningRate slr = this.getFeatureWiseLearningRate(featureId);
    return slr.learningRate;

  }

  @Override
  public double pollLearningRate(int agentTime, int featureId) {

    if (!useStateWise) {
      double oldVal = this.universalLR;
      if (agentTime > this.lastPollTime) {
        this.universalLR = this.nextLRVal(oldVal);
        this.lastPollTime = agentTime;
      }
      return oldVal;
    }

    StateWiseLearningRate slr = this.getFeatureWiseLearningRate(featureId);
    double oldVal = slr.learningRate;
    if (agentTime > slr.lastPollTime) {
      slr.learningRate = this.nextLRVal(oldVal);
      slr.lastPollTime = agentTime;
    }
    return oldVal;

  }

  @Override
  public void resetDecay() {
    this.universalLR = this.initialLearningRate;
    this.stateWiseMap.clear();
    this.featureWiseMap.clear();
  }

  /**
   * Returns the learning rate data structure for the given state. An entry will be created if it does not already exist.
   *
   * @param s the state to get a learning rate for
   * @return the learning rate data structure for the given state
   */
  protected StateWiseLearningRate getStateWiseLearningRate(State s) {
    HashableState sh = this.hashingFactory.hashState(s);
    StateWiseLearningRate slr = this.stateWiseMap.get(sh);
    if (slr == null) {
      slr = new StateWiseLearningRate();
      this.stateWiseMap.put(sh, slr);
    }
    return slr;
  }

  /**
   * Returns the learning rate data structure for the given state feature. An entry will be created if it does not already exist.
   *
   * @param feature the state feature id to get a learning rate for
   * @return the learning rate data structure for the given state feature
   */
  protected StateWiseLearningRate getFeatureWiseLearningRate(int feature) {
    StateWiseLearningRate slr = this.featureWiseMap.get(feature);
    if (slr == null) {
      slr = new StateWiseLearningRate();
      this.featureWiseMap.put(feature, slr);
    }
    return slr;
  }

  /**
   * Returns the value of an input current learning rate after it has been decayed by one time step.
   *
   * @param cur the currently learning rate to be decayed by one time step.
   * @return the value of an input current learning rate after it has been decayed by one time step.
   */
  protected double nextLRVal(double cur) {
    return Math.max(cur * decayRate, this.minimumLR);
  }

  /**
   * A class for storing a learning rate for a state, or a learning rate for each action for a given state
   *
   * @author James MacGlashan
   *
   */
  protected class StateWiseLearningRate {

    double learningRate;
    Map<String, MutableDouble> actionLearningRates = null;
    int lastPollTime = -1;

    public StateWiseLearningRate() {
      this.learningRate = initialLearningRate;
      if (useStateActionWise) {
        this.actionLearningRates = new HashMap<String, MutableDouble>();
      }
    }

    /**
     * Returns the mutable double entry for the learning rate for the action for the state with which this object is associated.
     *
     * @param ga the input action for which the learning rate is returned.
     * @return the mutable double entry for the learning rate for the action for the state with which this object is associated.
     */
    public MutableDouble getActionLearningRateEntry(Action ga) {
      MutableDouble entry = this.actionLearningRates.get(ga);
      if (entry == null) {
        entry = new MutableDouble(initialLearningRate);
        this.actionLearningRates.put(ga.actionName(), entry);
      }
      return entry;
    }

  }

  /**
   * A class for storing a mutable double value object
   *
   * @author James MacGlashan
   *
   */
  protected class MutableDouble {

    double md;
    int lastPollTime = -1;

    public MutableDouble(double md) {
      this.md = md;
    }
  }

}
