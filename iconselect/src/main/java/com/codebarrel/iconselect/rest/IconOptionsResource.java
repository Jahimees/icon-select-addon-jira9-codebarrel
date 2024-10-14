package com.codebarrel.iconselect.rest;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.codebarrel.iconselect.api.IconOptionBean;
import com.codebarrel.iconselect.api.IconOptionsService;
import com.codebarrel.iconselect.api.PositionBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/option")
@Produces({"application/json"})
@Consumes({"application/json"})
@Scanned
public class IconOptionsResource {
    private final IconOptionsService iconOptionsService;

    private final JiraAuthenticationContext authenticationContext;

    @Autowired
    public IconOptionsResource(IconOptionsService iconOptionsService, JiraAuthenticationContext authenticationContext) {
        this.iconOptionsService = iconOptionsService;
        this.authenticationContext = authenticationContext;
    }

    @GET
    @Path("/{id}")
    public Response getOption(@PathParam("id") Long id) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.getIconOptionById(loggedInUser, id);
        return serviceOutcome.fold(
            option -> Response.ok(option).build(),
            errors -> toErrorResponse((ErrorCollection) errors));
    }

    @GET
    @Path("/context/{fieldConfig}/{id}")
    public Response getOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.getIconOptionByFieldConfigAndId(loggedInUser, fieldConfigId, id);
        return serviceOutcome.fold(
            option -> Response.ok(option).build(),
            errors -> toErrorResponse((ErrorCollection) errors));
    }

    @GET
    @Path("/context/{fieldConfig}")
    public Response getAllOptionsForContext(@PathParam("fieldConfig") Long fieldConfigId) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceOutcome<List<IconOptionBean>> serviceOutcome = this.iconOptionsService.getAllIconOptionForFieldConfig(loggedInUser, fieldConfigId);
        return serviceOutcome.fold(
            options -> Response.ok(options).build(),
            errors -> toErrorResponse((ErrorCollection) errors));
    }

    @POST
    @Path("/context/{fieldConfig}")
    public Response createIconOption(@PathParam("fieldConfig") Long fieldConfigId, IconOptionBean optionBean) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.createIconOption(loggedInUser, fieldConfigId, optionBean);
        return serviceOutcome.fold(
            option -> Response.status(Response.Status.CREATED).entity(option).build(),
            IconOptionsResource::toErrorResponse);
    }

    @PUT
    @Path("/context/{fieldConfig}/{id}")
    public Response updateIconOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id, IconOptionBean optionBean) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        optionBean.setId(id);
        ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.updateIconOption(loggedInUser, fieldConfigId, optionBean);
        return serviceOutcome.fold(option -> Response.ok(option).build(),
            errors -> toErrorResponse((ErrorCollection) errors));
    }

    @DELETE
    @Path("/context/{fieldConfig}/{id}")
    public Response deleteIconOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceResult serviceOutcome = this.iconOptionsService.deleteIconOption(loggedInUser, fieldConfigId, id);
        if (!serviceOutcome.isValid())
            return toErrorResponse((ErrorCollection) serviceOutcome.getErrorCollection());
        return Response.ok().build();
    }

    @POST
    @Path("/context/{fieldConfig}/{id}/move")
    public Response moveIconOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id, PositionBean positionBean) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        if (positionBean.getPosition() != null) {
            if (positionBean.getPosition().equals("First")) {
                ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.moveToPosition(loggedInUser, fieldConfigId, id, 0);
                return serviceOutcome.fold(
                    option -> Response.ok(option).build(),
                    IconOptionsResource::toErrorResponse);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("Position supplied but was not 'First'.").build();
        }
        if (positionBean.getAfter() != null) {
            String after = positionBean.getAfter();
            String strRelativeId = after.substring(after.lastIndexOf('/') + 1);
            try {
                Long relativeId = Long.valueOf(Long.parseLong(strRelativeId));
                ServiceOutcome<IconOptionBean> serviceOutcome = this.iconOptionsService.moveToAfter(loggedInUser, fieldConfigId, id, relativeId);
                return serviceOutcome.fold(
                    option -> Response.ok(option).build(),
                    errors -> toErrorResponse((ErrorCollection) errors));
            } catch (NumberFormatException nfe) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Relative must be in form of .../relativeId").build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Position must be supplied as '{after: 'relative option'} or {position:'First'}").build();
    }

    @POST
    @Path("/context/{fieldConfig}/{id}/disable")
    public Response disableOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceResult serviceOutcome = this.iconOptionsService.disableIconOption(loggedInUser, fieldConfigId, id);
        if (!serviceOutcome.isValid())
            return toErrorResponse((ErrorCollection) serviceOutcome.getErrorCollection());
        return Response.ok().build();
    }

    @POST
    @Path("/context/{fieldConfig}/{id}/enable")
    public Response enableOption(@PathParam("fieldConfig") Long fieldConfigId, @PathParam("id") Long id) {
        ApplicationUser loggedInUser = this.authenticationContext.getLoggedInUser();
        ServiceResult serviceOutcome = this.iconOptionsService.enableIconOption(loggedInUser, fieldConfigId, id);
        if (!serviceOutcome.isValid())
            return toErrorResponse((ErrorCollection) serviceOutcome.getErrorCollection());
        return Response.ok().build();
    }

    private static Response toErrorResponse(ErrorCollection errors) {
        ErrorCollection restErrors = new SimpleErrorCollection();
        restErrors.addErrorCollection(errors);

        return Response.status(restErrors
                .getReasons()
                .iterator()
                .next()
                .getHttpStatusCode())
            .entity(restErrors).build();
    }
}
