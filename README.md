# City-Path-Finder
Class Assignment from Artificial Intelligence. 

Required arguments while running the program include: the file name for the cities/their latitudes/longitudes and the initial/goal city. Initial city is marked by "-i", goal city is marked by "-g", and the file name is marked by "-f". All of these flags need to be followed by a space before their respective names. 

Optional arguments while running the program include: "-s" for the search strategy ("uniform-cost", "greedy", otherwise, defaults to A* search) , "-h" to use haversine heuristic function (otherwise, defaults to euclidean), "--no-reached" to indicate _not_ to use a reached table (otherwise, defaults to true), and "-v" for the verbosity level ("0", "1", or "2", defaults to 0).

Example run: java CitySearch -f cities02.csv -i "La Crosse" -g "Winona" -v "1"

Expected output: * Reading data from [cities02.csv]
* Number of cities: 758
* Searching for path from La Crosse to Winona using a-star search 
* Goal found  : Winona  (p->La Crosse) [f= 28.2; g= 28.2; h= 0.0]
* Search took 750ms

Route found: La Crosse -> Winona
Distance: 28.2

Total nodes generated 	   : 113
Nodes remaining on frontier: 111

