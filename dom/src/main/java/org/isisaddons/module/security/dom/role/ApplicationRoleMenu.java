/*
 *  Copyright 2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.module.security.dom.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;

import org.isisaddons.module.security.SecurityModule;
import org.isisaddons.module.security.dom.role.ApplicationRoles.AllRolesDomainEvent;
import org.isisaddons.module.security.util.LDAPUtil;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "100.20"
)
public class  ApplicationRoleMenu {

    //region > domain event classes
    public static class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationRoleMenu, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationRoleMenu, T> {}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationRoleMenu> {}
    //endregion

    //region > iconName
    public String iconName() {
        return "applicationRole";
    }
    //endregion

	public Map<String, String> properties;

	@Programmatic
	@PostConstruct
	public void init(final Map<String, String> properties) {
		this.properties = properties;
	}
	
    //region > findRoles
    public static class FindRolesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = FindRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.20.1")
    public List<? extends ApplicationRole> findRoles(
            @Parameter(maxLength = ApplicationRole.MAX_LENGTH_NAME)
            @ParameterLayout(named = "Search", typicalLength = ApplicationRole.TYPICAL_LENGTH_NAME)
            final String search) {
        return applicationRoleRepository.findNameContaining(search);
    }
    //endregion

    //region > newRole
    public static class NewRoleDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = NewRoleDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(sequence = "100.20.2")
    public ApplicationRole newRole(
            @Parameter(maxLength = ApplicationRole.MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=ApplicationRole.TYPICAL_LENGTH_NAME)
            final String name,
            @Parameter(maxLength = JdoColumnLength.DESCRIPTION, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Description", typicalLength=ApplicationRole.TYPICAL_LENGTH_DESCRIPTION)
            final String description) {
        return applicationRoleRepository.newRole(name, description);
    }
    //endregion

    //region > allRoles
    public static class AllRolesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.20.3")
    public List<? extends ApplicationRole> allRoles() {
        return applicationRoleRepository.allRoles();
    }
    //endregion

    
 // region > ListAllRoleByRezaDomainEvent (action)
    public static class ListAllRoleByRezaDomainEvent extends ActionDomainEvent {}
    @Action(
            domainEvent = ListAllRoleByRezaDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.20.4")
 	public List<ApplicationRoleV> listAllRoleByReza() {
 		List<ApplicationRoleV> roles = new ArrayList<ApplicationRoleV>();
 		LdapContext ldapContext = null;
 		try {
 			ldapContext = LDAPUtil.getLDAPContext();

 			SearchControls sc = new SearchControls();
 			String[] attributeFilter = { "cn", "roleOccupant" };
 			sc.setReturningAttributes(attributeFilter);
 			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

 			NamingEnumeration<SearchResult> searchResults = ldapContext
 					.search(properties.get("isis.prime.ldap.groupSearchBase"), "(cn=*)", sc);

 			while (searchResults.hasMore()) {
 				SearchResult searchResult = (SearchResult) searchResults.next();
 				Attributes attributes = searchResult.getAttributes();

 				Attribute cnAttribute = attributes.get("cn");
 				Attribute roleOccupantAttribute = attributes.get("roleOccupant");
 				ApplicationRoleV appRole = new ApplicationRoleV();
 				appRole.setName(extractRoleName(cnAttribute));
 				appRole.setDescription(appRole.getName());
 						/*new AppRole(
 						cnAttribute != null && cnAttribute.get() != null ? cnAttribute.get().toString() : "",
 						extractRoleName(cnAttribute), mementoService);*/
 				/*Set<String> roleOccupantsSet = new HashSet<String>();
 				if (roleOccupantAttribute != null) {
 					NamingEnumeration<?> all = roleOccupantAttribute.getAll();
 					while (all != null && all.hasMore()) {
 						Object next = all.next();
 						roleOccupantsSet.add(userManagement.findUserByUid((String) next));
 					}
 				}*/
 				// appRole.setMembers(roleOccupantsSet.toArray(new
 				// AppUser[]{}));
 				/*appRole.setMembers(roleOccupantsSet);*/
 				roles.add(appRole);
 				/*if (LOG.isInfoEnabled())
 					LOG.info("Role found on LdapRealm: " + appRole);*/
 			}
 		} catch (NamingException e) {
 			e.printStackTrace();
 			/*if (LOG.isErrorEnabled())
 				LOG.error("LdapRealm operation for searching user's email failed: " + e.getMessage(), e);*/

 		} finally {
 			LDAPUtil.closeLdapContext(ldapContext);
 		}
 		return roles;
 	}
	/**
	 * 
	 */
	private String extractRoleName(Attribute attr) throws NamingException {
		if (attr != null && attr.get() != null)
			return attr.getID() + "=" + attr.get().toString() + "," + properties.get("isis.prime.ldap.groupSearchBase");
		else
			return null;
	}
 	// endregion
    @Inject
    ApplicationRoleRepository applicationRoleRepository;
}
