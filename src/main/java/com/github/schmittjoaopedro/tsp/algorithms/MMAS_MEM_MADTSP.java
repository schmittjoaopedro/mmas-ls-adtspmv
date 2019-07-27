package com.github.schmittjoaopedro.tsp.algorithms;

import com.github.schmittjoaopedro.statistic.GlobalStatistics;
import com.github.schmittjoaopedro.statistic.IterationStatistic;
import com.github.schmittjoaopedro.tsp.aco.MMAS;
import com.github.schmittjoaopedro.tsp.aco.MMAS_MEM_Memory;
import com.github.schmittjoaopedro.tsp.graph.Graph;
import com.github.schmittjoaopedro.tsp.graph.Vertex;
import com.github.schmittjoaopedro.tsp.tools.DBGP;
import com.github.schmittjoaopedro.tsp.tools.MVBS;
import com.github.schmittjoaopedro.tsp.utils.Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Max-Min Ant System with Memory for the Asymmetric and Dynamic Travelling Salesman Problem with Moving Vehicle
 */
public class MMAS_MEM_MADTSP implements Runnable {

    private MVBS mvbs;

    private DBGP dbgp;

    private Graph graph;

    private MMAS mmas;

    private MMAS_MEM_Memory memory;

    private int maxIterations;

    private int statisticInterval = 1;

    private int mmasSeed;

    private int dbgpSeed;

    private double rho;

    private double magnitude;

    private int frequency;

    private boolean showLog = true;

    private boolean useLocalSearch = false;

    private boolean changed = false;

    private double alpha;

    private double beta;

    private List<IterationStatistic> iterationStatistics;

    private GlobalStatistics globalStatistics = new GlobalStatistics();

    public MMAS_MEM_MADTSP(Graph graph, double rho, int maxIterations, double magnitude, int frequency, double alpha, double beta) {
        this.maxIterations = maxIterations;
        this.rho = rho;
        this.alpha = alpha;
        this.beta = beta;
        this.magnitude = magnitude;
        this.frequency = frequency;
        this.graph = graph;
        mmas = new MMAS(graph);
        memory = new MMAS_MEM_Memory(graph, mmas);
        dbgp = new DBGP(graph);
        iterationStatistics = new ArrayList<>(maxIterations);
    }

    @Override
    public void run() {
        // Initialization DBGP
        globalStatistics.startTimer();
        dbgp.setLowerBound(0.0);
        dbgp.setUpperBound(2.0);
        dbgp.setRandom(new Random(getDbgpSeed()));
        dbgp.setMagnitude(magnitude);
        dbgp.setFrequency(frequency);
        dbgp.initializeEnvironment();
        globalStatistics.endTimer("DBGP Initialization");
        // Initialization MMAS
        Random random = new Random(getMmasSeed());
        globalStatistics.startTimer();
        mmas.setRho(rho);
        mmas.setAlpha(alpha);
        mmas.setBeta(beta);
        mmas.setQ_0(0.0);
        mmas.setnAnts(50);
        mmas.setDepth(20);
        mmas.setEPSILON(0.000000000000000000000001);
        mmas.allocateAnts();
        mmas.allocateStructures();
        mmas.setRandom(random);
        mmas.computeNNList();
        mmas.initHeuristicInfo();
        mmas.initTry();
        globalStatistics.endTimer("MMAS Initialization");
        // Initialization moving vehicle benchmark simulator
        globalStatistics.startTimer();
        mvbs = new MVBS(graph, mmas);
        mvbs.setMaxIterations(maxIterations);
        mvbs.initialize();
        globalStatistics.endTimer("MVBS Initialization");
        // Initialization memory
        globalStatistics.startTimer();
        memory.setShortMemorySize(4);
        memory.setMutationProbability(0.01);
        memory.setImmigrantRate(0.4);
        memory.setRandom(random);
        memory.setMvbs(mvbs);
        memory.initialize();
        globalStatistics.endTimer("MEMORY Initialization");
        // Execute MMAS
        globalStatistics.startTimer();
        for (int i = 1; i <= maxIterations; i++) {
            IterationStatistic iterationStatistic = new IterationStatistic();
            mmas.setCurrentIteration(i);
            // Construction
            iterationStatistic.startTimer();
            mmas.computeNNList();
            mmas.initHeuristicInfo();
            mmas.computeTotalInfo();
            mvbs.constructSolutions();
            iterationStatistic.endTimer("Construction");
            // Daemon
            iterationStatistic.startTimer();
            boolean hasBest = mmas.updateBestSoFar();
            if (hasBest) {
                if (useLocalSearch) {
                    mmas.setPheromoneBoundsForLS();
                } else {
                    mmas.setPheromoneBounds();
                }
            }
            mmas.updateRestartBest();
            iterationStatistic.endTimer("Daemon");
            // Pheromone
            iterationStatistic.startTimer();
            mmas.evaporation();
            if (changed) {
                mmas.setRho(0.02);
                memory.updateShortTermMemory();
                memory.pheromoneUpdate(i);
            } else {
                mmas.pheromoneUpdate();
            }
            if (useLocalSearch) {
                mmas.updateUGB();
            }
            mmas.checkPheromoneTrailLimits();
            mmas.searchControl();
            iterationStatistic.endTimer("Pheromone");
            mvbs.moveNext(i, mmas.getBestSoFar());
            // Statistics
            if (i % statisticInterval == 0) {
                iterationStatistic.setIteration(i);
                iterationStatistic.setBestSoFar(mmas.getBestSoFar().getCost());
                iterationStatistic.setDiversity(mmas.calculateDiversity());
                iterationStatistic.setBranchFactor(mmas.getCalculatedBranchFact());
                iterationStatistic.setIterationBest(mmas.findBest().getCost());
                iterationStatistic.setIterationWorst(mmas.findWorst().getCost());
                iterationStatistic.setIterationMean(Maths.getPopMean(mmas.getAntPopulation()));
                iterationStatistic.setIterationSd(Maths.getPopultionStd(mmas.getAntPopulation()));
                iterationStatistic.setTour(mmas.getBestSoFar().getTour().clone());
                iterationStatistic.setMvsbTour(mvbs.getTour().clone());
                iterationStatistics.add(iterationStatistic);
                if (showLog) {
                    System.out.println(iterationStatistic);
                }
            }
            if (i < maxIterations) {
                dbgp.applyNewChanges(i);
            }
            repairSolution();
        }
        globalStatistics.endTimer("MMAS Execution");
        globalStatistics.setBestSoFarTC(mmas.getBestSoFar().getCost());
        List<Vertex> tour = new ArrayList<>(graph.getVertexCount() + 1);
        for (int vertexId : mmas.getBestSoFar().getTour()) {
            tour.add(graph.getVertex(vertexId));
        }
        globalStatistics.setBestRoute(tour);
    }

    private void repairSolution() {
        double originalCost = mmas.getBestSoFar().getCost();
        mmas.getBestSoFar().setCost(mmas.fitnessEvaluation(mmas.getBestSoFar().getTour()));
        if (originalCost != mmas.getBestSoFar().getCost()) {
            changed = true;
        }
    }

    public List<IterationStatistic> getIterationStatistics() {
        return iterationStatistics;
    }

    public void setIterationStatistics(List<IterationStatistic> iterationStatistics) {
        this.iterationStatistics = iterationStatistics;
    }

    public GlobalStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public void setGlobalStatistics(GlobalStatistics globalStatistics) {
        this.globalStatistics = globalStatistics;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public int getStatisticInterval() {
        return statisticInterval;
    }

    public void setStatisticInterval(int statisticInterval) {
        this.statisticInterval = statisticInterval;
    }

    public int getMmasSeed() {
        return mmasSeed;
    }

    public void setMmasSeed(int mmasSeed) {
        this.mmasSeed = mmasSeed;
    }

    public int getDbgpSeed() {
        return dbgpSeed;
    }

    public void setDbgpSeed(int dbgpSeed) {
        this.dbgpSeed = dbgpSeed;
    }

    public MVBS getMvbs() {
        return mvbs;
    }

    public void setMvbs(MVBS mvbs) {
        this.mvbs = mvbs;
    }
}
