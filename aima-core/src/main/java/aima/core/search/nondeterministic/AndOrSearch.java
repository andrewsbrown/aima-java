package aima.core.search.nondeterministic;
import aima.core.agent.Action;
import aima.core.agent.State;
import aima.core.search.framework.Metrics;
import java.util.Set;

/**
 * Implements an AND-OR search tree with conditional plans according to the
 * algorithm explained on pages 135-136 of AIMAv3. Unfortunately, this class 
 * cannot implement the interface Search (core.search.framework.Search) because
 * Search.search() returns a list of Actions to perform, whereas a 
 * nondeterministic search must return a Plan.
 * @author Andrew Brown
 */
public class AndOrSearch {

    protected int expandedNodes;

    /**
     * Searches through state space and returns a conditional plan for the 
     * given problem. The conditional plan is a list of either an action or
     * an if-then construct (consisting of a list of states and consequent
     * actions). The final product, when printed, resembles the contingency
     * plan on page 134.
     * 
     * This function is equivalent to the following on page 136:
     * 
     * <pre><code>
     * function AND-OR-GRAPH-SEARCH(problem) returns a conditional plan, or failure
     *  OR-SEARCH(problem.INITIAL-STATE, problem, [])
     * </pre></code>
     * 
     * @param problem
     * @return
     * @throws Exception 
     */
    public Plan search(NondeterministicProblem problem) throws Exception {
        this.expandedNodes = 0;
        // OR-SEARCH(problem.INITIAL-STATE, problem, [])
        return this.or_search(problem.getInitialState(), problem, new Path());
    }

    /**
     * Returns a conditional plan or null on failure; this function is equivalent
     * to the following on page 136:
     * 
     * <pre><code>
     * function OR-SEARCH(state, problem, path) returns a conditional plan, or failure
     *  if problem.GOAL-TEST(state) then return the empty plan
     *  if state is on path then return failure
     *  for each action in problem.ACTIONS(state) do
     *      plan = AND-SEARCH(RESULTS(state, action), problem, [state|path])
     *      if plan != failure then return [action|plan]
     *  return failure
     * </pre></code>
     * 
     * @param state
     * @param problem
     * @param path
     * @return 
     */
    public Plan or_search(Object state, NondeterministicProblem problem, Path path) {
        // do metrics
        this.expandedNodes++;
        // if problem.GOAL-TEST(state) then return the empty plan
        if (problem.isGoalState(state)) {
            return new Plan();
        }
        // if state is on path then return failure
        if (path.contains(state)) {
            return null;
        }
        // for each action in problem.ACTIONS(state) do
        for (Action action : problem.getActionsFunction().actions(state)) {
            // plan = AND-SEARCH(REQSULTS(state, action), problem, [state|path])
            Plan plan = this.and_search(problem.getResultsFunction().results(state, action), problem, path.prepend(state));
            // if plan != failure then return [action|plan]
            if (plan != null) {
                return plan.prepend(action);
            }
        }
        // return failure
        return null;
    }

    /**
     * Returns a conditional plan or null on failure; this function is equivalent
     * to the following on page 136:
     * 
     * <pre><code>
     * function AND-SEARCH(states, problem, path) returns a conditional plan, or failure
     *  for each s_i in states do
     *      plan_i = OR-SEARCH(s_i, problem, path)
     *      if plan_i == failure then return failure
     *  return [if s_1 then plan_1 else ... if s_n-1 then plan_n-1 else plan_n]
     * </pre></code>
     * 
     * @param states
     * @param problem
     * @param path
     * @return 
     */
    public Plan and_search(Set<Object> states, NondeterministicProblem problem, Path path) {
        // do metrics
        this.expandedNodes++;
        IfThen if_then = new IfThen();
        // for each s_i in states do
        for (Object s : states) {
            // plan_i = OR-SEARCH(s_i, problem, path)
            Plan plan = this.or_search(s, problem, path);
            if_then.add(s, plan);
            // if plan_i == failure then return failure
            if (plan == null) {
                return null;
            }
        }
        //return [[if s_1 then plan_1 else ... if s_n-1 then plan_n-1 else plan_n]
        return new Plan(if_then);
    }

    /**
     * Returns all the metrics of the node expander.
     * @return all the metrics of the node expander.
     */
    public Metrics getMetrics() {
        Metrics result = new Metrics();
        result.set("expandedNodes", this.expandedNodes);
        return result;
    }
}
