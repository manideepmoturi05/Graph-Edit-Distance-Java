package no.roek.nlpged.application;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.FileReader;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.util.Scanner;
import java.lang.*;
import javax.swing.JOptionPane;
import java.util.*;

import no.roek.nlpged.algorithm.GraphEditDistance;
import no.roek.nlpged.graph.Graph;
import no.roek.nlpged.graph.Node;
import no.roek.nlpged.misc.EditWeightService;
import no.roek.nlpged.preprocessing.DependencyParser;

import org.maltparser.core.exception.MaltChainedException;

import com.konstantinosnedas.HungarianAlgorithm;


public class App {

	public static void main(String[] args) throws MaltChainedException, ClassNotFoundException, IOException {
		Config cs = new Config("app.properties");
		DependencyParser depParser = new DependencyParser(cs.getProperty("MALT_PARAMS"), cs.getProperty("POSTAGGER_PARAMS"));
		Map<String, Double> posEditWeights = EditWeightService.getEditWeights(cs.getProperty("POS_SUB_WEIGHTS"), cs.getProperty("POS_INSDEL_WEIGHTS"));
		Map<String, Double> deprelEditWeights = EditWeightService.getInsDelCosts(cs.getProperty("DEPREL_INSDEL_WEIGHTS"));
		boolean running = true;
		
		String text1="", text2="", strLine;
		int i;
		
		Scanner inFile1 = new Scanner(new File("cleaned tweets.txt"));//text file name
		List<String> temps = new ArrayList<String>();
		while (inFile1.hasNextLine()) {
      			// find next line
      			strLine = inFile1.nextLine();
      			temps.add(strLine);
    		}
    		inFile1.close();

   		String[] tempsArray = temps.toArray(new String[temps.size()]);
		
		//int j=0;
		//double Normal_dist;
		//float inLine, follower_count;
		//Scanner inFile2 = new Scanner(new File("follow_count.txt"));
		//float [] tempsArray2 = new float[];
		//while (inFile2.hasNextFloat()) {
      			// find next line
      		//	tempsArray2[j++] = inFile2.nextFloat();
      			//temps2.add(inLine);
    		//}
    		//inFile2.close();

   		//int[] tempsArray2 = temps2.toArray(new int[temps2.size()]);
	
    		while(running) {
			System.out.println("\n------");
			for (i=0;i<1000000;i++) {//no.of lines in the text file
				text1 = tempsArray[i];
				text2 = ("Cellphone service shut down in #Boston to prevent remote detonations of explosives"); 			
				String[] texts = {text1,text2};
				//follower_count = tempsArray2[i];
				GraphEditDistance ged = getDistance(texts, depParser, posEditWeights, deprelEditWeights);
				System.out.println("Distance: "+ged.getDistance()+". Normalised Distance: "+ged.getNormalizedDistance());
				//System.out.println(tempsArray[i]) ""+ged.getNormalizedDistance()+;
				//Normal_dist = ged.getDistance() / follower_count;
				//System.out.println(""+Normal_dist);

			
			}
			System.out.println("Exiting..");
		}
	}
	
	public static GraphEditDistance getDistance(String[] texts, DependencyParser depParser, Map<String,Double> posEditWeights, Map<String,Double> deprelEditWeights) throws MaltChainedException {
		Graph g1 = depParser.dependencyParse("1", texts[0]);
		Graph g2 = depParser.dependencyParse("2", texts[1]);

		GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);
		return ged;
		//System.out.println("Calculating graph edit distance for the two sentences:");
		//System.out.println(texts[0]);
		//System.out.println(texts[1]);
		//System.out.println("Distance between the two sentences: "+ged.getDistance()+". Normalised: "+ged.getNormalizedDistance());
		
	}


	public static List<String> getEditPath(Graph g1, Graph g2, double[][] costMatrix, boolean printCost) {
		return getAssignment(g1, g2, costMatrix, true, printCost);
	}

	public static List<String> getFreeEdits(Graph g1, Graph g2, double[][] costMatrix) {
		return getAssignment(g1, g2, costMatrix, false, false);
	}

	public static List<String> getAssignment(Graph g1, Graph g2, double[][] costMatrix, boolean editPath, boolean printCost) {
		List<String> editPaths = new ArrayList<>();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");

		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			String to = getEditPathAttribute(assignment[i][1], g2);

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			if(cost != 0 && editPath) {
				if(printCost) {
					editPaths.add("("+from+" -> "+to+") = "+cost);
				}
			}else if(cost == 0 && !editPath) {
				editPaths.add("("+from+" -> "+to+")");
			}
		}

		return editPaths;

	}

	private static String getEditPathAttribute(int nodeNumber, Graph g) {
		if(nodeNumber < g.getNodes().size()) {
			Node n= g.getNode(nodeNumber);
			return n.getLabel();
		}else {
			return "ε";
		}
	}
	
}
