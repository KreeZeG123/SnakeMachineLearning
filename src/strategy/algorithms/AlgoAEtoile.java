package strategy.algorithms;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.ItemType;
import utils.Position;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AlgoAEtoile {

    public static Position algo(Position cible, Snake snake, SnakeGame game, Set<Position> avoid) {
        // Initialisation
        List<Node> aOuvrir = new ArrayList<>();
        List<Node> dejaVu = new ArrayList<>();
        int maxX = game.getSizeX();
        int maxY = game.getSizeY();
        Position snakeHeadPos = snake.getPositions().get(0);

        // Vérifie si la tête du serpent est déjà sur la cible (cas où me snake n'a pas pu manger la pomme a cause de la sickness)
        if (snakeHeadPos.equals(cible)) {
            // Recherche des voisins libres
            List<Position> voisinsLibres = generateValidNeighbors(snakeHeadPos, avoid, maxX, maxY);

            // S'il y a des voisins libres, on déplace le serpent vers l'un d'eux
            if (!voisinsLibres.isEmpty()) {
                // Trouve le voisin le moins proche du bord
                return trouverVoisinMoinsProcheBord(voisinsLibres, maxX, maxY);
            } else {
                // Si aucun voisin libre n'est trouvé, retourner la position actuelle
                return null;
            }
        }

        // Ajoute le noeud inital
        int initialDist = DistManhattan.distManhattan(snakeHeadPos, cible);
        if ( game.shouldUseManhattanWrap(snake) ) {
            initialDist = DistManhattan.distManhattanWrap(snakeHeadPos, cible, maxX, maxY);
        }
        Node initialNode = new Node(null, snakeHeadPos, 0,initialDist);
        aOuvrir.add(initialNode);
        int temporaire = 0;
        // Algo
        while (!aOuvrir.isEmpty()) {
            // Trie les noeuds à ouvrir en fonction du coût total (coût réel + coût heuristique)
            aOuvrir.sort(Comparator.comparingInt(a -> a.getRealCost() + a.getHeuristicCost()));

            // Premier noeud de la liste (coût total le plus bas)
            Node currentNode = aOuvrir.remove(0);
            int newRealCost = currentNode.getRealCost() + 1;

            // Ajouter les voisins du noeud courant à la liste des noeuds à ouvrir
            for ( Position p : generateValidNeighbors(currentNode.getPos(), avoid, maxX, maxY) ) {
                // Calcule du cout heuristic selon la distance de manhattan
                int newHeuristicCost = DistManhattan.distManhattan(p, cible);
                if ( game.shouldUseManhattanWrap(snake) ) {
                    newHeuristicCost = DistManhattan.distManhattanWrap(p, cible, maxX, maxY);
                }

                // Création du nouveau noeud voisin
                Node newNode = new Node(currentNode, p, newRealCost, newHeuristicCost);

                // Vérifie si ce nouveau noeud correspond a la cible (condition d'arret)
                if (newNode.getPos().equals(cible)) {
                    Node prochainNoued = deroulerParent(newNode, initialNode);
                    return (prochainNoued.getPos());
                }

                // Recherche si ce noeud existe déja
                Node rechercheNoeud = noeuxExiste(p,dejaVu);
                if ( rechercheNoeud == null ) {
                    rechercheNoeud = noeuxExiste(p,aOuvrir);
                }
                // Si le noeud existe déja
                if ( rechercheNoeud != null ) {
                    // On update le parent si le nouveau chemin est plus court
                    if ( newRealCost + newHeuristicCost < rechercheNoeud.getHeuristicCost() + rechercheNoeud.getRealCost() ) {
                        rechercheNoeud.setParent(currentNode);
                        rechercheNoeud.setHeuristicCost(newHeuristicCost);
                        rechercheNoeud.setRealCost(newRealCost);
                    }
                }
                // Sinon on ajoute ce noeud a la liste de noeud a ouvrir
                else {
                    aOuvrir.add(newNode);
                }
            }

            dejaVu.add(currentNode);
            temporaire++;
        }

        // Renvoie null si la cible n'a pas pu être atteinte durant le parcours en largeur a*
        return null;
    }

    /**
     * Génère les voisins valides d'une position donnée en excluant ceux qui sont dans "avoid".
     */
    private static List<Position> generateValidNeighbors(Position pos, Set<Position> avoid, int maxX, int maxY) {
        List<Position> validNeighbors = new ArrayList<>();

        // Positions voisines possibles (haut, bas, gauche, droite)
        Position[] neighbors = new Position[] {
                new Position(pos.getX(), pos.getY() - 1), // haut
                new Position(pos.getX(), pos.getY() + 1), // bas
                new Position(pos.getX() - 1, pos.getY()), // gauche
                new Position(pos.getX() + 1, pos.getY())  // droite
        };

        // Vérifie chaque voisin, et ajoute-le à la liste si ce n'est pas dans "avoid" et dans les limites du plateau
        for (Position neighbor : neighbors) {
            // Vérifier si la position est dans les limites du plateau et pas dans "avoid"
            if (    neighbor.getX() >= 0 && neighbor.getX() < maxX && neighbor.getY() >= 0 &&
                    neighbor.getY() < maxY  && !avoid.contains(neighbor)
                )
            {
                validNeighbors.add(neighbor);
            }
        }

        return validNeighbors;
    }

    private static Node noeuxExiste(Position pos, List<Node> list) {
        for (Node node : list) {
            if ( node.getPos().equals(pos) ) {
                return node;
            }
        }
        return null;
    }

    private static Node deroulerParent(Node noeud, Node noeudFinal) {
        // Vérifie que le noeud a déroulé ne soit pas déja le noeud final
        if ( noeud == noeudFinal ) {
            return noeud;
        }

        // Déroule les parents pour atteindre le noeud final
        Node noeudCourant = noeud;
        int tempo = 0;
        while ( noeudCourant.getParent() != noeudFinal ) {
            noeudCourant = noeudCourant.getParent();
            tempo++;
        }
        return noeudCourant;
    }

    public static void main(String[] args) {

    }

    // Trouve le voisin le moins proche du bord.
    private static Position trouverVoisinMoinsProcheBord(List<Position> voisins, int maxX, int maxY) {
        Position meilleurVoisin = null;
        int distanceMin = Integer.MAX_VALUE;

        for (Position voisin : voisins) {
            // Calcul de la distance à l'un des bords du plateau
            int distanceBord = Math.min(Math.min(voisin.getX(), maxX - voisin.getX()),
                    Math.min(voisin.getY(), maxY - voisin.getY()));

            // Choisir le voisin le moins proche du bord
            if (distanceBord < distanceMin) {
                distanceMin = distanceBord;
                meilleurVoisin = voisin;
            }
        }

        return meilleurVoisin;
    }

    public static Position findNearestApple(SnakeGame state, Position startPos) {
        Position nearestApple = null;
        int minDist = Integer.MAX_VALUE;

        for (Item item : state.getItems()) {
            if (item.getItemType() == ItemType.APPLE) {
                Position itemPose = new Position(item.getX(), item.getY());
                int dist = DistManhattan.distManhattan(startPos, itemPose);
                if (dist < minDist) {
                    minDist = dist;
                    nearestApple = itemPose;
                }
            }
        }
        return nearestApple;
    }


    public static Set<Position> generateAvoidSet(SnakeGame game, boolean includeSickBalls) {
        Set<Position> avoid = new HashSet<>();
        avoid.addAll(game.getWallsPositions());
        avoid.addAll(game.getSnakesPositions());
        if (includeSickBalls) {
            avoid.addAll(game.getItemsPositions(ItemType.SICK_BALL));
        }
        return avoid;
    }
}