package com.asana;

import static org.junit.Assert.assertEquals;

import com.asana.errors.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class ClientTest extends AsanaTest
{
    @Test
    public void testClientGet() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 200, "{ \"data\": { \"name\": \"me\" }}");
        assertEquals(client.users.me().execute().name, "me");
    }

    @Test
    public void testNotAuthorized() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 401, "{ \"errors\": [{ \"message\": \"Not Authorized\" }]}");
        try {
            client.users.me().execute();
        } catch (NoAuthorizationError error) {
            return;
        }
        Assert.fail("expected NoAuthorizationError to be thrown");
    }

    @Test
    public void testInvalidRequest() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 400, "{ \"errors\": [{ \"message\": \"Missing input\" }] }");
        try {
            client.users.me().execute();
        } catch (InvalidRequestError error) {
            return;
        }
        Assert.fail("expected InvalidRequestError to be thrown");
    }

    @Test
    public void testServerError() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 500, "{ \"errors\": [ { \"message\": \"Server Error\", \"phrase\": \"6 sad squid snuggle softly\" } ] }");
        try {
            client.users.me().execute();
        } catch (ServerError error) {
            return;
        }
        Assert.fail("expected ServerError to be thrown");
    }

    @Test
    public void testNotFound() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 404, "{ \"errors\": [ { \"message\": \"user: Unknown object: 1234\" } ] }");
        try {
            client.users.me().execute();
        } catch (NotFoundError error) {
            return;
        }
        Assert.fail("expected NotFoundError to be thrown");
    }

    @Test
    public void testForbidden() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me", 403, "{ \"errors\": [ { \"message\": \"user: Forbidden\" } ] }");
        try {
            client.users.me().execute();
        } catch (ForbiddenError error) {
            return;
        }
        Assert.fail("expected ForbiddenError to be thrown");
    }

    @Test
    public void testOptionPretty() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me?opt_pretty=true", 200, "{ \"data\": { \"name\": \"me\" } }");
        assertEquals(client.users.me()
                .option("pretty", true)
                .execute().name,
                "me");
    }

    @Test
    public void testOptionPrettyPOST() throws IOException
    {
        dispatcher.registerResponse("POST", "http://app/tasks", 200, "{ \"data\": { \"name\": \"task\" } }");
        assertEquals(client.tasks.create()
                .option("pretty", true)
                .execute().name,
                "task");
        assertEquals(dispatcher.calls.get(0).requestBody, "{\"data\":{},\"options\":{\"pretty\":true}}");
    }

    @Test
    public void testOptionFields() throws IOException
    {
        dispatcher.registerResponse("GET", "http://app/users/me?opt_fields=name,notes", 200, "{ \"data\": { \"name\": \"me\" } }");
        assertEquals(client.users.me()
                .option("fields", Arrays.asList("name", "notes"))
                .execute().name,
                "me");
    }

    @Test
    public void testOptionFieldsPOST() throws IOException
    {
        dispatcher.registerResponse("POST", "http://app/tasks", 200, "{ \"data\": { \"name\": \"task\" } }");
        assertEquals(client.tasks.create()
                .option("fields", Arrays.asList("name", "notes"))
                .execute().name,
                "task");
        assertEquals(dispatcher.calls.get(0).requestBody, "{\"data\":{},\"options\":{\"fields\":[\"name\",\"notes\"]}}");
    }

    @Test
    public void testOptionExpand() throws IOException
    {
        dispatcher.registerResponse("PUT", "http://app/tasks/1001", 200, "{ \"data\": { \"name\": \"me\" } }");
        assertEquals(client.tasks.update("1001")
                .data("assignee", "1234")
                .option("expand", Arrays.asList("projects"))
                .execute().name,
                "me");
        assertEquals(dispatcher.calls.get(0).requestBody, "{\"data\":{\"assignee\":\"1234\"},\"options\":{\"expand\":[\"projects\"]}}");
    }
}
