//Claudia Rush
import java.io.File;
import java.util.Comparator;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class CitySearch {
	
	 private static class Node implements Comparable<Node>{
		private String cityName;
		private double pathCost;
		private double heuristicFunction;
		private double nodeEvalFunction;
		private Node parent;
		
		private Node(String city, Node parent, double pathCost, double heuristicFunction, 
				double nodeEvalFunction) {
			cityName = city;
			this.parent = parent;
			this.pathCost = pathCost;
			this.heuristicFunction = heuristicFunction;
			this.nodeEvalFunction = nodeEvalFunction;
		}

		public int compareTo(CitySearch.Node o) {
			return 0;
		}

	 }
	 
	 private static class cityComparatorGreedy implements Comparator<Node>{
		public int compare(Node node1, Node node2) {
			if(node1.heuristicFunction > node2.heuristicFunction) {
				return 1;
			}
			else if(node1.heuristicFunction < node2.heuristicFunction) {
				return -1;
			}
			return 0;
		} 
	 }
	 
	 private static class cityComparatorUCS implements Comparator<Node>{ 
			public int compare(Node node1, Node node2) {
				if(node1.pathCost > node2.pathCost) {
					return 1;
				}
				else if(node1.pathCost < node2.pathCost) {
					return -1;
				}
				return 0;
			} 
	 }
	 
	 private static class cityComparatorAStar implements Comparator<Node>{
		 public int compare(Node node1, Node node2) {
			if(node1.nodeEvalFunction > node2.nodeEvalFunction) {
				return 1;
			}
			else if(node1.nodeEvalFunction < node2.nodeEvalFunction) {
				return -1;
			}
			return 0;
		} 
	 }

	 public static void main(String[] args) throws FileNotFoundException{
		 long startTime = System.currentTimeMillis();
		 String fileName = "";	
		 String initialCity = "";	
		 String goalCity = "";
		 String searchStrategy = "a-star";
		 String heuristic = "haversine";
		 boolean useReachedTable = true;
		 int verbosityLevel = 0;
		
		 for(int i=0; i<args.length; i++) {
			 if(args[i].equals("-f")) {
				 fileName = args[i+1];
			 }
			 else if(args[i].equals("-i")) {
				 initialCity = args[i+1];
			 }
			 else if(args[i].equals("-g")) {
				 goalCity = args[i+1];
			 }
			 else if(args[i].equals("-s")) {
				 searchStrategy = args[i+1];
			 }
			 else if(args[i].equals("-h")){
				 heuristic = args[i+1];
			 }
			 else if(args[i].equals("--no-reached")) {
				 useReachedTable = false;
			 }
			 else if(args[i].equals("-v")) {
				 verbosityLevel = Integer.parseInt(args[i+1]);
			 }
		 }
		
		 HashMap<String, double[]> citiesMap = new HashMap<String, double[]>();
		 HashMap<String, Double> interCityDistances = new HashMap<String, Double>();
		
		File file = new File(fileName);
		Scanner scan = new Scanner(file);
		scan.nextLine();	//scan first line, which is a comment
		while(scan.hasNextLine()) {
			String fileLine = scan.nextLine();
			if(fileLine.startsWith("#")) {
				break;
			}
			String[] citiesList = splitLine(fileLine);
			
			double[] coordinates = new double[] {Double.parseDouble(citiesList[1]),
					Double.parseDouble(citiesList[2])};
			citiesMap.put(citiesList[0], coordinates);
			
		}
		if(verbosityLevel >= 1) {
			System.out.println("* Reading data from [" + file + "]");
			System.out.println("* Number of cities: " + citiesMap.size());
			System.out.println("* Searching for path from " + initialCity + 
					" to " + goalCity + " using " + searchStrategy + " search ");
		}
		
		//need to check if the input file has the initial and goal cities
		if(!(citiesMap.keySet().contains(initialCity)
				&& citiesMap.keySet().contains(goalCity))) {
			System.out.println("One or more cities not found in the file!");
			System.exit(0);
		}
		
		while(scan.hasNextLine()) {	//continue reading file, make the inter city distances map
			String[] cityDistances = splitLine(scan.nextLine());
			interCityDistances.put(cityDistances[0] + "->" +cityDistances[1], 
					Double.parseDouble(cityDistances[2]));
			interCityDistances.put(cityDistances[1] + "->" +cityDistances[0], 
			Double.parseDouble(cityDistances[2]));	//because the distance goes in either direction
		}
		scan.close();
		
		String findPath = BestFirstSearch(citiesMap, interCityDistances, initialCity, goalCity, 
				searchStrategy, useReachedTable, heuristic, verbosityLevel, startTime);
		System.out.println(findPath);
		
	}
	
	public static String[] splitLine(String line) {
		String[] parts = line.split(",");
		String str1 = parts[0].trim();
		String str2 = parts[1].trim();
		String str3 = parts[2].trim();
		String[] string = new String[] {str1, str2, str3};
		return string;
	}
	
	public static String BestFirstSearch(HashMap<String, double[]> citiesMap, 
			HashMap<String, Double> interCityMap, String initialCity, String goalCity, String searchStrategy, 
			boolean useReachedTable, String heuristic, int verbosityLevel, long startTime) {
		double h = ActionCostHeuristic(citiesMap, initialCity, goalCity, heuristic);
		Node root = new Node(initialCity, null, 0, h, h);	
		int totalNodes = 0; //will be used later in output
		//determine which search strategy to use
		PriorityQueue<Node> frontier;
		if(searchStrategy.compareTo("greedy") == 0) {
			frontier = new PriorityQueue<>(new cityComparatorGreedy());
		}
		else if(searchStrategy.compareTo("uniform-cost") == 0) {
			frontier = new PriorityQueue<>(new cityComparatorUCS());
		}
		else {	//a-star search
			frontier = new PriorityQueue<>(new cityComparatorAStar());
		}
		
		frontier.add(root);
		totalNodes++;
		HashMap<String, Node> reachedTable = new HashMap<String, Node>();
		if(useReachedTable) {
			reachedTable.put(root.cityName, root);
		}	
		
		while(!frontier.isEmpty()) {
			Node node = frontier.remove();
			if(verbosityLevel >= 2) {
				if(node.parent != null) {
					System.out.printf("  Expanding   : %s  (p->%s) [f= %.1f; g= %.1f; h= %.1f]\n", 
							node.cityName, node.parent.cityName, node.nodeEvalFunction, 
							node.pathCost, node.heuristicFunction);
				}
				else {
					System.out.printf("  Expanding   : %s  (p->null) [f= %.1f; g= %.1f; h= %.1f]\n", 
							node.cityName, node.nodeEvalFunction, node.pathCost, 
							node.heuristicFunction);
				}
			}
			if(node.cityName.compareTo(goalCity)==0) {
				return printOutput(node, verbosityLevel, totalNodes, frontier, startTime);
			}
			for(Node child: Expand(citiesMap, interCityMap, node, goalCity,
					searchStrategy, heuristic)) {
				String cityName = child.cityName;
				if(!(reachedTable.containsKey(cityName)) || 
						child.pathCost < reachedTable.get(cityName).pathCost) {
					if(useReachedTable) {
						reachedTable.put(cityName, child);
					}
					frontier.add(child);
					totalNodes++;
					if(verbosityLevel == 3) {
						System.out.printf("    Adding    : %s  (p->%s) [f= %.1f; g= %.1f; h= %.1f]\n", 
								child.cityName, child.parent.cityName, child.nodeEvalFunction, 
								child.pathCost, child.heuristicFunction);
					}
				}
				else {
					if(verbosityLevel == 3) {
						System.out.printf("    NOT Adding    : %s  (p->%s) [f= %.1f; g= %.1f; h= %.1f]\n", 
								child.cityName, child.parent.cityName, child.nodeEvalFunction, 
								child.pathCost, child.heuristicFunction);
					}	
				}
			}
		}
		return "NO PATH";
	}
	
	public static PriorityQueue<Node> Expand(HashMap<String, double[]> citiesMap,
			HashMap<String, Double> interCityMap, Node node, String goalCity, 
			String searchStrategy, String heuristic){
		PriorityQueue<Node> childNodes = new PriorityQueue<Node>();
		if(searchStrategy.compareTo("uniform") == 0) {
			childNodes = new PriorityQueue<Node>(new cityComparatorUCS());
		}
		else if(searchStrategy.compareTo("greedy") == 0) {
			childNodes = new PriorityQueue<Node>(new cityComparatorGreedy());
		}
		else {
			childNodes = new PriorityQueue<Node>(new cityComparatorAStar());
		}
		
		String cityName = node.cityName;
		Set<String> actionSet = Actions(interCityMap, cityName);
		for(String action: actionSet) {
			String nextCity = action;
			double pathCost = 0;
			double heuristicValue = 0;
			double nodeEvalFunction = 0;
			if(searchStrategy.compareTo("greedy") == 0) {
				pathCost = node.pathCost + ActionCost(interCityMap, cityName, nextCity);
				heuristicValue = ActionCostHeuristic(citiesMap, nextCity, goalCity, heuristic);
			}
			else if(searchStrategy.compareTo("a-star") == 0){
				pathCost = node.pathCost + ActionCost(interCityMap, cityName, nextCity);
				heuristicValue = ActionCostHeuristic(citiesMap, nextCity, goalCity, heuristic);
				nodeEvalFunction = heuristicValue + pathCost;
			}
			else {
				pathCost = node.pathCost + ActionCost(interCityMap, cityName, nextCity);
			}
			
			Node nextNode = new Node(nextCity, node, pathCost, heuristicValue, nodeEvalFunction);
			childNodes.add(nextNode);
		}
		return childNodes;
	}
	
	public static Set<String> Actions(HashMap<String, Double> interCityMap,
			String cityName) { //returns set of cities current city can move to
		Set<String> actionSet = new HashSet<String>();
		for(String city: interCityMap.keySet()) {
			if(city.split("->")[0].compareTo(cityName)==0) { //look for actions to take from city
				actionSet.add(city.split("->")[1]);
			}
		}
		return actionSet;	
	}
	
	public static double ActionCost(HashMap<String, Double> interCitiesMap, String cityName,
			String nextCityName) {
		return interCitiesMap.get(cityName + "->" + nextCityName);
	}
	
	public static double ActionCostHeuristic(HashMap<String, double[]> citiesMap,
			String cityName, String nextCityName, String heuristic) {	
		if(heuristic.compareTo("haversine") == 0) {
			return haversineDistance(citiesMap, cityName, nextCityName);
		}
		else {
			return euclideanDistance(citiesMap, cityName, nextCityName);
		}
	}
	
	public static double euclideanDistance(HashMap<String, double[]> citiesMap, 
			String cityName, String nextCityName) {
		double[] coords = getCoords(citiesMap, cityName, nextCityName);
		
		double latDifference = Math.abs(coords[2] - coords[0]);
		double squareLatDiff = Math.pow(latDifference, 2);
		double longDifference = Math.abs(coords[3] - coords[1]);
		double squareLongDiff = Math.pow(longDifference, 2);
		double sum = squareLatDiff + squareLongDiff;
		double sqrtSum = Math.sqrt(sum);
		return sqrtSum;
	}
	
	public static double haversineDistance(HashMap<String, double[]> citiesMap,
			String cityName, String nextCityName) {
		double[] coords = getCoords(citiesMap, cityName, nextCityName);
		double cityNameLat = coords[0] * (Math.PI/180);
		double cityNameLong = coords[1] * (Math.PI/180);
		double nextCityNameLat = coords[2] * (Math.PI/180);
		double nextCityNameLong = coords[3] * (Math.PI/180);
		
		double deltaLong = nextCityNameLong - cityNameLong;
		double deltaLat = nextCityNameLat - cityNameLat;
		double a = Math.pow((Math.sin(deltaLat/2)), 2) + Math.cos(cityNameLat) 
				* Math.cos(nextCityNameLat) * Math.pow((Math.sin(deltaLong/2)), 2);
		double c = 2 * (Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
		double d = 3958.8 * c;
		return d;
	}
	
	public static double[] getCoords(HashMap<String, double[]> citiesMap, String cityName,
			String nextCityName) {
		double[] coordinates = new double[4];
		coordinates[0] = citiesMap.get(cityName)[0];	//lat 1
		coordinates[1] = citiesMap.get(cityName)[1];	//long 1
		coordinates[2] = citiesMap.get(nextCityName)[0];	//lat 2
		coordinates[3] = citiesMap.get(nextCityName)[1];	//long 2
		return coordinates;
	}
	
	public static String printOutput(Node goalNode, int verbosityLevel, int totalNodes,
			PriorityQueue<Node> frontier, long startTime) {
		if(verbosityLevel >= 1) {
			if(goalNode.parent!= null) {
				System.out.printf("* Goal found  : %s  (p->%s) [f= %.1f; g= %.1f; h= %.1f]\n", 
						goalNode.cityName, goalNode.parent.cityName, goalNode.nodeEvalFunction,
						goalNode.pathCost, goalNode.heuristicFunction);
			}
			else {
				System.out.printf("* Goal found  : %s  (p->null) [f= %.1f; g= %.1f; h= %.1f]\n", 
						goalNode.cityName, goalNode.nodeEvalFunction,
						goalNode.pathCost, goalNode.heuristicFunction);
			}
			System.out.println("* Search took " + (System.currentTimeMillis()-startTime) + "ms\n");
		}

		List<String> path = new ArrayList<>();
		path.add(goalNode.cityName);
		while(goalNode.parent != null) {
			path.add(goalNode.parent.cityName + " -> ");
			goalNode.parent = goalNode.parent.parent;
		}
		Collections.reverse(path);
		String output = "Route found: " + String.join("", path);
		output += "\nDistance: " + goalNode.pathCost + "\n";
		output += "\nTotal nodes generated 	   : " + totalNodes;
		output += "\nNodes remaining on frontier: " + frontier.size();

		return output;
	}
}