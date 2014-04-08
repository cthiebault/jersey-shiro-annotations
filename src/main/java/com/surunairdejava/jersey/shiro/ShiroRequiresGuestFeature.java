package com.surunairdejava.jersey.shiro;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.surunairdejava.jersey.shiro.CglibProxyUtils.isSuperMethodAnnotated;

@Provider
public class ShiroRequiresGuestFeature implements DynamicFeature {

  private static final Logger log = LoggerFactory.getLogger(ShiroRequiresGuestFeature.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Class<?> resourceClass = resourceInfo.getResourceClass();
    Method resourceMethod = resourceInfo.getResourceMethod();
    if(resourceClass.isAnnotationPresent(RequiresGuest.class) ||
        resourceClass.getSuperclass().isAnnotationPresent(RequiresGuest.class) ||
        resourceMethod.isAnnotationPresent(RequiresGuest.class) ||
        isSuperMethodAnnotated(resourceClass.getSuperclass(), resourceMethod, RequiresGuest.class)) {
      log.debug("Register RequiresGuestRequestFilter for {}", resourceInfo);
      context.register(new RequiresGuestRequestFilter());
    }
  }

  @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
  private static class RequiresGuestRequestFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if(SecurityUtils.getSubject().getPrincipal() != null) {
        throw new ForbiddenException();
      }
    }
  }

}
