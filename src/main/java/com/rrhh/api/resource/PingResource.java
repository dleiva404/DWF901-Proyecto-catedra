
package com.rrhh.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.rrhh.api.filter.Publico;

import java.util.Collections;
import java.util.Map;

@Path("/ping")
public class PingResource {

    @GET
    @Publico
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> ping() {
        return Collections.singletonMap("status", "ok");
    }
}
