////////////////////////////////////////////////////////////////////////////////
//  This file is part of MABED.                                               //
//                                                                            //
//  MABED is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  MABED is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with MABED.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

package fr.ericlab.mabed.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.ConnectedComponents.ConnectedComponent;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class EventGraph {
    public Graph graph;
    public Graph redundancyGraph;
    public HashMap<String,Event> duplicatedEvents;
    double maximumScore;
    double sigma;
    Corpus corpus;
    
    public EventGraph(Corpus d, double ms, Double sig){
        sigma = sig;
        graph = new SingleGraph("");
        redundancyGraph = new SingleGraph("");
        maximumScore = ms;
        corpus = d;
        duplicatedEvents = new HashMap<>();
    }
    
    public int addEvent(Event event){
        int added = 0;
        boolean redundant = false;
        if(graph.getNode(event.mainTerm) != null){
            for(WeightedTerm wt : event.relatedTerms.list){
                Node wtNode = graph.getNode(wt.term);
                if(wtNode != null){
                    if(wtNode.getAttribute("ui.class").equals("mainTerm") && wtNode.hasEdgeFrom((Node)graph.getNode(event.mainTerm))){
                        Event event1 = getEvent(wtNode);
                        double intersection = Math.max(event.I.intersectionProportion(event1.I), event1.I.intersectionProportion(event.I));
                        if(intersection > sigma){
                            redundant = true;
                            duplicatedEvents.put(event.mainTerm,event);
                            duplicatedEvents.put(event1.mainTerm,event1);
                            // new way of managing redundancy
                            if(redundancyGraph.getNode(event1.mainTerm) == null){
                                redundancyGraph.addNode(event1.mainTerm);
                            }
                            if(redundancyGraph.getNode(event.mainTerm) == null){
                                redundancyGraph.addNode(event.mainTerm);
                            }
                            if(redundancyGraph.getEdge(event.mainTerm+"-"+event1.mainTerm) == null){
                                redundancyGraph.addEdge(event.mainTerm+"-"+event1.mainTerm, event.mainTerm, event1.mainTerm, false);
                            }
                        }
                    }
                }
            }
        }
        if(!redundant){
            if(event.mainTerm != null){
                if(graph.getNode(event.mainTerm) == null){
                    graph.addNode(event.mainTerm);
                }
                graph.getNode(event.mainTerm).addAttributes(event.getMainTermAttributes());
                graph.getNode(event.mainTerm).setAttribute("ui.size",20+(event.score/maximumScore)*10);
                graph.getNode(event.mainTerm).addAttribute("ui.label", "["+corpus.toDate(event.I.timeSliceA)+"::"+corpus.toDate(event.I.timeSliceB)+"]:"+event.mainTerm);
                graph.getNode(event.mainTerm).addAttribute("anomaly",event.anomaly);
                for(WeightedTerm wt : event.relatedTerms.list){
                    if(wt.term != null){
                        if(graph.getNode(wt.term)==null){
                            graph.addNode(wt.term);
                            graph.getNode(wt.term).addAttribute("ui.label", wt.term);
                            graph.getNode(wt.term).setAttribute("ui.class","relatedTerm");
                        }
                        graph.addEdge("["+event.I.timeSliceA+":"+event.I.timeSliceB+"]"+event.mainTerm+"-"+wt.term,wt.term,event.mainTerm,true);
                        graph.getEdge("["+event.I.timeSliceA+":"+event.I.timeSliceB+"]"+event.mainTerm+"-"+wt.term).addAttribute("weight", wt.weight);
                    }
                }
            }
            added = 1;
        }
        return added;
    }
    
    public EventList identifyConnectedComponents(){
        ConnectedComponents ccs = new ConnectedComponents(redundancyGraph);
        ccs.setCountAttribute("component");
        System.out.println("   - number of connected component(s): "+ccs.getConnectedComponentsCount());
        int i = 0;
        EventList globalEvents = new EventList();
        for (ConnectedComponent cc : ccs) {
            EventList ccEvents = new EventList();
            for(Node node : cc.getEachNode()){
                Event event = duplicatedEvents.get(node.getId());
                ccEvents.add(event);
                removeEvent(event);
            }
            ccEvents.sort();
            Event mainEvent = ccEvents.list.pop();
            Event globalEvent = mainEvent.merge(ccEvents);
            globalEvents.add(globalEvent);
            addEvent(globalEvent);
            i++;
        }
        return globalEvents;
    }
    
    public void removeEvent(Event event){
        Node mainNode = graph.getNode(event.mainTerm);
        if(mainNode != null){
            if(mainNode.getAttribute("ui.class").equals("mainTerm")){
                Collection<Edge> edges = mainNode.getEnteringEdgeSet();
                if(edges != null){
                    for(Edge edge : edges){
                        if(edge != null){
                            // remove obsolete edge
                            graph.removeEdge(edge);
                            if(edge.getSourceNode().getDegree() == 0){
                                graph.removeNode((Node)edge.getSourceNode());
                            }
                        }
                    }
                }
                if(mainNode.getOutDegree() == 0){
                    graph.removeNode(mainNode);
                }else{
                    mainNode.setAttribute("ui.class", "relatedTerm");
                }
            }
        }
    }
    
    public void replaceEvent(Node nodeT0, Event t1){
        nodeT0.setAttribute("ui.class","relatedTerm");
        Collection<Edge> edges = nodeT0.getEnteringEdgeSet();
        graph.addNode(t1.mainTerm);
        graph.getNode(t1.mainTerm).addAttributes(t1.getMainTermAttributes());
        graph.getNode(t1.mainTerm).setAttribute("ui.size",20+(t1.score/maximumScore)*10);
        graph.getNode(t1.mainTerm).addAttribute("ui.label", "["+corpus.toDate(t1.I.timeSliceA)+"::"+corpus.toDate(t1.I.timeSliceB)+"]:"+t1.mainTerm);        
        graph.getNode(t1.mainTerm).setAttribute("anomaly",t1.anomaly);
        if(edges != null){
            for(Edge edge : edges){
                if(edge != null){
                    // remove obsolete edge
                    graph.removeEdge(edge);
                    // add edge toward the new main term
                    if(!t1.mainTerm.contains(edge.getSourceNode().getId())){
                        String edgeId = "["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+edge.getSourceNode().getId();
                        graph.addEdge(edgeId,edge.getSourceNode().getId(),t1.mainTerm,true);
                        graph.getEdge(edgeId).addAttribute("weight", edge.getAttribute("weight"));
                    }
                    if(edge.getSourceNode().getDegree() == 0){
                        graph.removeNode((Node)edge.getSourceNode());
                    }
                }
            }
        }
        graph.removeNode(nodeT0);
        for(WeightedTerm wt : t1.relatedTerms.list){
            if(wt.term != null){
                if(graph.getNode(wt.term)==null){
                    graph.addNode(wt.term);
                    graph.getNode(wt.term).addAttribute("ui.label", wt.term);
                    graph.getNode(wt.term).setAttribute("ui.class","relatedTerm");
                }
                if(graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term) == null){
                    graph.addEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term,wt.term,t1.mainTerm,true);
                }
                graph.getEdge("["+t1.I.timeSliceA+":"+t1.I.timeSliceB+"]"+t1.mainTerm+"-"+wt.term).addAttribute("weight", wt.weight);
            }
        }
//        if(nodeT0.getDegree() == 0){
//            System.out.println("   - "+nodeT0.getId()+" is a leaf node.");
//            graph.removeNode(nodeT0);
//        } 
   }
    
    public Event getEvent(Node mainNode){
        Event event = new Event();
        if(mainNode.getAttribute("ui.class")!=null && mainNode.getAttribute("ui.class").equals("mainTerm")){
            event = new Event(mainNode.getId(), new TimeInterval((String) mainNode.getAttribute("I")), (double) mainNode.getAttribute("score"), (ArrayList<Double>) mainNode.getAttribute("anomaly"));
            for(Edge edge : mainNode.getEnteringEdgeSet()){
                event.relatedTerms.add(new WeightedTerm(edge.getSourceNode().getId(), (double) edge.getAttribute("weight")));
            }
        }
        return event;
    }
    
    public EventList toEventList(){
        EventList events = new EventList();
        for(Node node : graph){
            if(node.getAttribute("ui.class")!=null && node.getAttribute("ui.class").equals("mainTerm")){
                Event event = new Event(node.getId(), new TimeInterval((String) node.getAttribute("I")), (double) node.getAttribute("score"), (ArrayList<Double>) node.getAttribute("anomaly"));
                for(Edge edge : node.getEnteringEdgeSet()){
                    event.relatedTerms.add(new WeightedTerm(edge.getSourceNode().getId(), (double) edge.getAttribute("weight")));
                }
                events.add(event);
            }
        }
        events.sort();
        return events;
    }
    
}
