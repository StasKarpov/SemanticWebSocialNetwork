package com.assigment2.rest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.base.Sys;
import org.apache.jena.update.UpdateAction;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.html.View;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import javax.servlet.http.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.vocabulary.DC ;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;



@Path("/")
public class SocialMediaService {

    public SocialMediaService() throws IOException{
        readModel();
    }

    public String ass1 = " PREFIX ass1:<http://www.univie.ac.at/mst/ass1#>";
    public String ma = " PREFIX ma:<http://www.w3.org/ns/ma-ont#>";
    public String owl = " PREFIX owl:<http://www.w3.org/ns/ma-ont#>";
    public String foaf = " PREFIX foaf:<http://xmlns.com/foaf/0.1/>";
    public String rdf = " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    public String rdfs = " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
    Model model = ModelFactory.createDefaultModel();


    //reads rdf model
    private void readModel(){
        //open rdf file
        // InputStream in = new URL("http://moodle.univie.ac.at/pluginfile.php/3817241/mod_resource/content/1/social-media-default.rdf").openStream();
        InputStream in = FileManager.get().open("/socialmedia.rdf");
        if(in==null){
            in = this.getClass().getResourceAsStream("/socialmedia.rdf");
        }
        model.read(in, "");
    }

    String executeSelect(String query){
        String queryString =ass1 + ma + owl + foaf + rdf + rdfs + query ;

        QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(queryString), model);
        ResultSet results = qe.execSelect();
        return ResultSetFormatter.asXMLString(results);
    }

    void executeUpdate(String query){
        String queryString =ass1 + ma + owl + foaf + rdf + rdfs + query;
        UpdateAction.parseExecute(queryString,model);
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response home(){
        return Response.status(200).entity("<h2>Hi!<br>This is my Social Media RESTful web service.</h2><br>"+
                                            "<h3>Author: Stanislav Karpov 11738423<br>" +
                                            "<h4>Default graph is uploaded.</h4>").build();
    }

    @GET // This annotation indicates GET request
    @Path("/displayModel")
    @Produces(MediaType.TEXT_PLAIN)
    public Response displayModel() {
        OutputStream out = new ByteArrayOutputStream();
        model.write(out);
        String output = out.toString();
        return Response.status(200).entity(output).build();
    }


    @GET
    @Path("/persons")
    @Produces(MediaType.TEXT_XML)
    public Response persons() {
        String stringOutput = executeSelect("SELECT ?persons WHERE {?persons rdf:type ass1:User }");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }

    @GET
    @Path("/persons/{id}")
    @Produces(MediaType.TEXT_XML)
    public Response personById(@PathParam("id") String id) {

        String stringOutput = executeSelect("SELECT ?property ?value WHERE {ass1:"+ id +" ?property ?value}");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }

    @POST
    @Path("/persons/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPersonInJSON(String person, @PathParam("id") String id) {
        System.out.println("hi");
        executeUpdate(" INSERT DATA { ass1:" + id + " rdf:type ass1:User, owl:NamedIndividual }");

        try {
            Object obj = new JSONParser().parse(person);
            JSONObject jo = (JSONObject) obj;
            for(Object k: jo.keySet()){
               executeUpdate("INSERT DATA { ass1:" + id + " " + k.toString()+ " \"" +jo.get(k) + "\".} ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.status(200).build();
    }

    @PUT
    @Path("/persons/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePersonInJSON(String person, @PathParam("id") String id) {
        try {
            Object obj = new JSONParser().parse(person);
            JSONObject jo = (JSONObject) obj;
            for(Object k: jo.keySet()){
                executeUpdate("UPDATE DATA { ass1:" + id + " " + k.toString()+ " \"" +jo.get(k) + "\".} ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.status(200).build();
    }

    @DELETE
    @Path("/persons/{id}")
    public Response deletePersonInJSON(@PathParam("id") String id) {
        //System.out.print("INSERT DATA { ass1:" + id + " " + k.toString()+ " \"" +jo.get(k) + "\".} " );
        executeUpdate("DELETE WHERE {ass1:"+ id + " ?p ?a } ");
        return Response.status(200).build();
    }

    @GET
    @Path("/comments")
    @Produces(MediaType.TEXT_XML)
    public Response comments() {
        String stringOutput = executeSelect("SELECT * WHERE {?comment rdf:type ass1:Comment. ?comment ?property ?value }");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }

    @GET
    @Path("/persons/{id}/comments")
    @Produces(MediaType.TEXT_XML)
    public Response commentsOfPerson(@PathParam("id") String id) {
        String stringOutput = executeSelect("SELECT * WHERE {?comment ass1:hasAuthor ass1:"+id+"." +
                                                                    "?comment rdf:type ass1:Comment."+
                                                                    "?comment ?property ?value }");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }

    @GET
    @Path("/media/{id}/comments")
    @Produces(MediaType.TEXT_XML)
    public Response commentsForMedia(@PathParam("id") String id) {
        String stringOutput = executeSelect("SELECT ?text WHERE {?comments ass1:isCommentOf ass1:"+id+"." +
                                                                "?comments ass1:text ?text }");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }

    @POST
    @Path("/comments/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCommentInJSON(String comment, @PathParam("id") String id) {
        System.out.println("hi");
        executeUpdate(" INSERT DATA { ass1:" + id + " rdf:type ass1:Comment, owl:NamedIndividual }");

        try {
            Object obj = new JSONParser().parse(comment);
            JSONObject jo = (JSONObject) obj;
            for(Object k: jo.keySet()){
                System.out.print("INSERT DATA { ass1:" + id + " " + k.toString()+ " " +jo.get(k) + ".} ");
                executeUpdate("INSERT DATA { ass1:" + id + " " + k.toString()+ " " +jo.get(k) + ".} ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.status(200).build();
    }

    @PUT
    @Path("/comments/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCommentInJSON(String comment, @PathParam("id") String id) {
        try {
            Object obj = new JSONParser().parse(comment);
            JSONObject jo = (JSONObject) obj;

            for(Object k: jo.keySet()){
                executeUpdate("UPDATE DATA { ass1:" + id + " " + k.toString()+ " \"" +jo.get(k) + "\".} ");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.status(200).build();
    }

    @DELETE
    @Path("/comment/{id}")
    public Response deleteCommentInJSON(@PathParam("id") String id) {
        //System.out.print("INSERT DATA { ass1:" + id + " " + k.toString()+ " \"" +jo.get(k) + "\".} " );
        executeUpdate("DELETE WHERE {ass1:"+ id + " ?p ?a } ");
        return Response.status(200).build();
    }

    @GET
    @Path("/media")
    @Produces(MediaType.TEXT_XML)
    public Response media() {
        String stringOutput = executeSelect("SELECT ?media ?property ?value WHERE {?mediatypes rdfs:subClassOf ma:MediaResource."+
                                            " ?media rdf:type ?mediatypes. ?media ?property ?value  }");
        System.out.print(stringOutput);
        // Output query results
        return Response.status(200).entity(stringOutput).build();
    }









    public static void main (String args[]) throws IOException {

        SocialMediaService hrs = new SocialMediaService();
        hrs.media();





    }

}