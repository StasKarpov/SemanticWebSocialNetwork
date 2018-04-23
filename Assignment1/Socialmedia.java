
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.vocabulary.DC ;

import java.io.*;

/** Tutorial 5 - read RDF XML from a file and write it to standard out
 */
public class Socialmedia extends Object {

    /**
        NOTE that the file is loaded from the class-path and so requires that
        the data-directory, as well as the directory containing the compiled
        class, must be added to the class-path when running this and
        subsequent examples.
    */
    static final String inputFileName  = "socialmedia.rdf";

    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        //open rdf file
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }

        // read the RDF/XML file
            model.read(in, "");


        //create prefixes
        String sm = " PREFIX sm:<http://www.semanticweb.org/pc/ontologies/2018/3/SK_MST_ontology#>";
        String ma = " PREFIX ma:<http://www.w3.org/ns/ma-ont#>";
        String owl = " PREFIX owl:<http://www.w3.org/ns/ma-ont#>";
        String foaf = " PREFIX foaf:<http://xmlns.com/foaf/0.1/>";
        String rdf = " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
        String rdfs = " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";

        //####################################sub-task 1
        //retrieve titles of all media objects of type TXT

        // create a string which contains query
        String queryString =sm + ma + owl + foaf +
            "SELECT ?title WHERE {?y sm:format \"txt\" . " +
                                "  ?y ma:title ?title }" ;

        Query query = QueryFactory.create(queryString) ;

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);

        ////####################################sub-task 2.1
        //retrieve all URIs of your friends

        //in this query it is assumed that name is an identical id but also we can
        //search by id or by sm:me which are actually identical
        queryString = sm + ma + owl + foaf + rdf +
          "SELECT ?myFriends WHERE {?x foaf:name \"Stanislav Karpov\" . " +
                                " ?x sm:friendWith  ?myFriends }" ;
        query = QueryFactory.create(queryString) ;

        qe = QueryExecutionFactory.create(query, model);
        results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);

        ////####################################sub-task 2.2
        //retrieve all URIs of friends of your friends

        //getting list of friends of my friends filtering me in this list
        queryString = sm + ma + owl + foaf + rdf +
          "SELECT ?friendsOfMyFriends WHERE {?x foaf:name \"Stanislav Karpov\" . " +
                                " ?x sm:friendWith  ?y . " +
                                " ?y sm:friendWith ?friendsOfMyFriends . "+
                                " FILTER (?friendsOfMyFriends != sm:me )}" ; // filtering sm:me
        query = QueryFactory.create(queryString) ;

        qe = QueryExecutionFactory.create(query, model);
        results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);

        ////####################################sub-task 3(with error)
        //retrieve all URIs of images with a minimum of two likes,
        // but there should not be any persons who liked and comments any image of the list

        //geting list of friends of my friends filtering me in this list
        queryString = sm + ma + owl + foaf + rdf +
          "SELECT ?images (count(distinct ?like) as ?likes)"
                      +" WHERE {?images rdf:type sm:Video . "+
                               "?like sm:liked ?images . " +
                               " { select ?i ?l where {?i ?images ?images . ?l ?likes ?likes .filter( ?l >= 2)} } "+ // sub query
                               " } GROUP BY ?images" ;
        query = QueryFactory.create(queryString) ;

        qe = QueryExecutionFactory.create(query, model);
        results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);

        ////####################################sub-task 4
        //retrieve all media objects, which their titles that start with "Franz”
        // ordered by creation dates ascending


        //getting list of friends of my friends filtering me in this list
        queryString = sm + ma + owl + foaf + rdf + rdfs +
          "SELECT ?titles ?dates WHERE {?x rdfs:subClassOf ma:MediaResource . " +
                           "?y rdf:type ?x . "+
                           "?y ma:title ?titles . "+
                           "OPTIONAL{?y sm:dateOfRelease ?dates .}"+
                           "FILTER regex(?titles, \"^Franz\", \"i\") } "+//using regex expression
                                                                         //for filtering
                           "ORDER BY ?dates" ;
        query = QueryFactory.create(queryString) ;

        qe = QueryExecutionFactory.create(query, model);
        results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);




        // Important - free up resources used running the query
        qe.close();


    }


}
