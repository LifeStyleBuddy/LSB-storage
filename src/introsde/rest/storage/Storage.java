package introsde.rest.storage;

import introsde.document.ws.PeopleService;
import introsde.document.ws.People;
import introsde.document.ws.Person;
import introsde.document.ws.Measure;
import introsde.document.ws.Goal;
import introsde.document.ws.ParseException_Exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.ws.Holder;

import javax.ejb.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.*;

import javax.xml.ws.Holder;

@Stateless
@LocalBean
@Path("/storage")
public class Storage {

	// -------------"SOAP local-DB-service request"------------------------
	// Important, it will work only if build and ivy file are the same as the
	// ones inside localdb service

	// 1) Getting person info
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/getPersonInfo/{personId}")
	public Response getPersonInfo(@PathParam("personId") int personId) throws ClientProtocolException, IOException {
		PeopleService service = new PeopleService();
		People people = service.getPeopleImplPort();
		Person p = people.readPerson(personId);

		if (p != null) {
			return Response.status(200).entity(p).build();
		}

		return Response.status(400).build();
	}

	// 2) Create new Person
	@POST
	@Path("/createPerson")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response createPerson(Person person) throws IOException {

		PeopleService service2 = new PeopleService();
		People people = service2.getPeopleImplPort();

		Holder<Person> holder = new Holder<Person>(person);

		people.createPerson(holder);

		Person newPerson = new Person();
		newPerson = holder.value;
		if (newPerson != null) {
			return Response.ok(newPerson).build();
		}
		return Response.status(400).build();
	}

	// 3) Create new Measure for a person
	@POST
	@Path("/newMeasure/{personId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response newMeasure(@PathParam("personId") int idPerson, Measure measure) throws IOException {

		PeopleService service3 = new PeopleService();
		People people = service3.getPeopleImplPort();
		
		Holder<Measure> me = new Holder<Measure>(measure);

		try {
			people.savePersonMeasure(idPerson, me);
		} catch (ParseException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		people.updatePersonMeasure(idPerson, me);

		Measure m = new Measure();
		m = me.value;

		if (m != null) {
			return Response.status(200).entity(m).build();
		} else
			return Response.status(400).build();
	}

	// 4) Create new Goal for a person
	@POST
	@Path("/newGoal/{personId}")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response newGoal(@PathParam("personId") int idPerson, Goal goal) throws IOException {

		PeopleService service3 = new PeopleService();
		People people = service3.getPeopleImplPort();

		Holder<Goal> me = new Holder<Goal>(goal);

		try {
			people.savePersonGoal(idPerson, me);
		} catch (ParseException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		people.updatePersonGoal(idPerson, me);

		Goal m = new Goal();
		m = me.value;

		if (m != null) {
			return Response.status(200).entity(m).build();
		} else
			return Response.status(400).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/persons/{personId}/measures")
	public List<Measure> getGoals(@PathParam("personId") int idPerson) {
		PeopleService service3 = new PeopleService();
		People people = service3.getPeopleImplPort();
		List<Measure> measures = people.readPersonHistory(idPerson, "test1");
		if (measures == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return measures;
	}

	// -------------"REST adapter request"------------------------
	// Getting a quote+photo from PIXABAY API
	@GET
	@Path("/getPicture")
	public Response getPicture() throws ClientProtocolException, IOException {
		String ENDPOINT = "https://radiant-plateau-33754.herokuapp.com/introsde/adapter/getPictureQuote";

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(ENDPOINT);
		HttpResponse response = client.execute(request);
		String jsonResponse;

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		JSONObject o = new JSONObject(result.toString());

		if (response.getStatusLine().getStatusCode() == 200) {

			String pictureUrl = "\"picture_url\":\"" + o.getString("web_format_url") + "\"";
			jsonResponse = "{" + pictureUrl + "}";

			return Response.ok(jsonResponse).build();
		}

		return Response.status(204).build();
	}

	// Getting a LUNCH RECIPE from EDAMAM API
	@GET
	@Path("/getLunchRecipe")
	public Response getLunchRecipe() throws ClientProtocolException, IOException {

		String ENDPOINT = "https://radiant-plateau-33754.herokuapp.com/introsde/adapter/getLunchRecipe";

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(ENDPOINT);
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		JSONObject o = new JSONObject(result.toString());

		if (response.getStatusLine().getStatusCode() == 200) {

			return Response.ok(o.toString()).build();
		}

		return Response.status(204).build();
	}

	// Getting a DINNER RECIPE from EDAMAM API
	@GET
	@Path("/getDinnerRecipe")
	public Response getDinnerRecipe() throws ClientProtocolException, IOException {

		String ENDPOINT = "https://radiant-plateau-33754.herokuapp.com/introsde/adapter/getDinnerRecipe";

		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(ENDPOINT);
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		JSONObject o = new JSONObject(result.toString());

		if (response.getStatusLine().getStatusCode() == 200) {

			return Response.ok(o.toString()).build();
		}

		return Response.status(204).build();
	}
}