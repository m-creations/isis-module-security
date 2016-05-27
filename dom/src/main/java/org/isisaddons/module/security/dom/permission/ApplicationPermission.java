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
package org.isisaddons.module.security.dom.permission;

import java.util.Comparator;
import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.util.ObjectContracts;

import org.isisaddons.module.security.SecurityModule;
import org.isisaddons.module.security.dom.feature.ApplicationFeature;
import org.isisaddons.module.security.dom.feature.ApplicationFeatureId;
import org.isisaddons.module.security.dom.feature.ApplicationFeatureRepository;
import org.isisaddons.module.security.dom.feature.ApplicationFeatureType;
import org.isisaddons.module.security.dom.feature.ApplicationMemberType;
import org.isisaddons.module.security.dom.role.ApplicationRole;
import org.isisaddons.module.security.dom.role.ApplicationRoleRepository;

/**
 * Specifies how a particular {@link #getRole() application role} may interact with a specific
 * {@link #getFeature() application feature}.
 *
 * <p>
 *     Each permission has a {@link #getRule() rule} and a {@link #getMode() mode}.  The
 *     {@link ApplicationPermissionRule rule} determines whether the permission {@link ApplicationPermissionRule#ALLOW grants}
 *     access to the feature or {@link ApplicationPermissionRule#VETO veto}es access
 *     to it.  The {@link ApplicationPermissionMode mode} indicates whether
 *     the role can {@link ApplicationPermissionMode#VIEWING view} the feature
 *     or can {@link ApplicationPermissionMode#CHANGING change} the state of the
 *     system using the feature.
 * </p>
 *
 * <p>
 *     For a given permission, there is an interaction between the {@link ApplicationPermissionRule rule} and the
 *     {@link ApplicationPermissionMode mode}:
 * <ul>
 *     <li>for an {@link ApplicationPermissionRule#ALLOW allow}, a
 *     {@link ApplicationPermissionMode#CHANGING usability} allow
 *     implies {@link ApplicationPermissionMode#VIEWING visibility} allow.
 *     </li>
 *     <li>conversely, for a {@link ApplicationPermissionRule#VETO veto},
 *     a {@link ApplicationPermissionMode#VIEWING visibility} veto
 *     implies a {@link ApplicationPermissionMode#CHANGING usability} veto.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("UnusedDeclaration")
@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isissecurity",
        table = "ApplicationPermission")
@Extension(vendorName = "datanucleus", key = "datastore", value = "store-security")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries( {
        @javax.jdo.annotations.Query(
                name = "findAll", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "),
        @javax.jdo.annotations.Query(
                name = "findByRole", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "
                        + "WHERE role == :role"),
        @javax.jdo.annotations.Query(
                name = "findByUser", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "
                        + "WHERE (u.roles.contains(role) && u.username == :username) "
                        + "VARIABLES org.isisaddons.module.security.dom.user.ApplicationUser u"),
        @javax.jdo.annotations.Query(
                name = "findByFeature", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "
                        + "WHERE featureType == :featureType "
                        + "   && featureFqn == :featureFqn"),
        @javax.jdo.annotations.Query(
                name = "findByRoleAndRuleAndFeature", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "
                        + "WHERE role == :role "
                        + "   && rule == :rule "
                        + "   && featureType == :featureType "
                        + "   && featureFqn == :featureFqn "),
        @javax.jdo.annotations.Query(
                name = "findByRoleAndRuleAndFeatureType", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.security.dom.permission.ApplicationPermission "
                        + "WHERE role == :role "
                        + "   && rule == :rule "
                        + "   && featureType == :featureType "),
})
@javax.jdo.annotations.Uniques({
        @javax.jdo.annotations.Unique(
                name = "ApplicationPermission_role_feature_rule_UNQ", members = { "role", "featureType", "featureFqn", "rule" })
})
@DomainObject(
        objectType = "isissecurity.ApplicationPermission"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
@MemberGroupLayout(
        columnSpans = {3,3,6,12},
        left="Role",
        middle = "Permissions",
        right="Feature"
)
public class ApplicationPermission implements Comparable<ApplicationPermission> {

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationPermission, T> {}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationPermission, T> {}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationPermission> {}

    // //////////////////////////////////////

    private static final int TYPICAL_LENGTH_TYPE = 7;  // ApplicationFeatureType.PACKAGE is longest

    //region > identification
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        final StringBuilder buf = new StringBuilder();
        buf.append(getRole().getName()).append(":")  // admin:
           .append(" ").append(getRule().toString()) // Allow|Veto
           .append(" ").append(getMode().toString()) // Viewing|Changing
           .append(" of ");

        final ApplicationFeatureId featureId = getFeatureId();
        switch (getFeatureType()) {
            case PACKAGE:
                buf.append(getFeatureFqn());              // com.mycompany
                break;
            case CLASS:
                // abbreviate if required because otherwise title overflows on action prompt.
                if(getFeatureFqn().length() < 30) {
                    buf.append(getFeatureFqn());          // com.mycompany.Bar
                } else {
                    buf.append(featureId.getClassName()); // Bar
                }
                break;
            case MEMBER:
                buf.append(featureId.getClassName())
                   .append("#")
                   .append(featureId.getMemberName());   // com.mycompany.Bar#foo
                break;
        }
        return buf.toString();
    }
    //endregion

    //region > role (property)

    public static class RoleDomainEvent extends PropertyDomainEvent<ApplicationRole> {}


    private ApplicationRole role;

    @javax.jdo.annotations.Column(name = "roleId", allowsNull="false")
    @Property(
            domainEvent = RoleDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden=Where.REFERENCES_PARENT
    )
    @MemberOrder(name="Role", sequence = "1")
    public ApplicationRole getRole() {
        return role;
    }

    public void setRole(final ApplicationRole role) {
        this.role = role;
    }

    //endregion

    //region > updateRole (action)
    public static class UpdateRoleDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateRoleDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="Role", sequence = "1")
    public ApplicationPermission updateRole(final ApplicationRole applicationRole) {
        setRole(applicationRole);
        return this;
    }

    public ApplicationRole default0UpdateRole() {
        return getRole();
    }
    
    public List<ApplicationRole> choices0UpdateRole() {
       return applicationRoleRepository.allRoles();
   }

    //endregion

    //region > rule (property)
    public static class RuleDomainEvent extends PropertyDomainEvent<ApplicationPermissionRule> {}

    private ApplicationPermissionRule rule;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = RuleDomainEvent.class,
            editing = Editing.DISABLED
    )
    @MemberOrder(name="Permissions", sequence = "2")
    public ApplicationPermissionRule getRule() {
        return rule;
    }

    public void setRule(final ApplicationPermissionRule rule) {
        this.rule = rule;
    }

    //endregion

    //region > allow (action)
    public static class AllowDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllowDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name = "Rule", sequence = "1")
    public ApplicationPermission allow() {
        setRule(ApplicationPermissionRule.ALLOW);
        return this;
    }
    public String disableAllow() {
        return getRule() == ApplicationPermissionRule.ALLOW? "Rule is already set to ALLOW": null;
    }

    //endregion

    //region > veto (action)
    public static class VetoDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = VetoDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name = "Rule", sequence = "1")
    public ApplicationPermission veto() {
        setRule(ApplicationPermissionRule.VETO);
        return this;
    }
    public String disableVeto() {
        return getRule() == ApplicationPermissionRule.VETO? "Rule is already set to VETO": null;
    }

    //endregion

    //region > mode (property)
    public static class ModeDomainEvent extends PropertyDomainEvent<ApplicationPermissionMode> {}

    private ApplicationPermissionMode mode;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = ModeDomainEvent.class,
            editing = Editing.DISABLED
    )
    @MemberOrder(name="Permissions", sequence = "3")
    public ApplicationPermissionMode getMode() {
        return mode;
    }

    public void setMode(final ApplicationPermissionMode mode) {
        this.mode = mode;
    }

    //endregion

    //region > viewing(action)

    public static class ViewingDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = ViewingDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name = "Mode", sequence = "1")
    public ApplicationPermission viewing() {
        setMode(ApplicationPermissionMode.VIEWING);
        return this;
    }
    public String disableViewing() {
        return getMode() == ApplicationPermissionMode.VIEWING ? "Mode is already set to VIEWING": null;
    }

    //endregion

    //region > changing (action)

    public static class ChangingDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = ChangingDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name = "Mode", sequence = "2")
    public ApplicationPermission changing() {
        setMode(ApplicationPermissionMode.CHANGING);
        return this;
    }
    public String disableChanging() {
        return getMode() == ApplicationPermissionMode.CHANGING ? "Mode is already set to CHANGING": null;
    }
    //endregion

    //region > featureId (derived property)

    private ApplicationFeatureId getFeatureId() {
        if(getFeatureType() == null) {
            return null;
        }
        return ApplicationFeatureId.newFeature(getFeatureType(), getFeatureFqn());
    }
    ApplicationFeature getFeature() {
        if(getFeatureId() == null) {
            return null;
        }
        return applicationFeatureRepository.findFeature(getFeatureId());
    }

    //endregion

    // region > type (derived, combined featureType and memberType)

    public static class TypeDomainEvent extends PropertyDomainEvent<String> {}

    /**
     * Combines {@link #getFeatureType() feature type} and member type.
     */
    @Property(
            domainEvent = TypeDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(typicalLength=ApplicationPermission.TYPICAL_LENGTH_TYPE)
    @MemberOrder(name="Feature", sequence = "5")
    public String getType() {
        final Enum<?> e = getFeatureType() != ApplicationFeatureType.MEMBER ? getFeatureType() : getMemberType();
        return e != null ? e.name(): null;
    }

    @Programmatic
    private ApplicationMemberType getMemberType() {
        final ApplicationFeature feature = getFeature();
        return feature != null? feature.getMemberType(): null;
    }
    //endregion

    //region > featureType

    @javax.jdo.annotations.Column(allowsNull="false")
    private ApplicationFeatureType featureType;

    /**
     * The {@link org.isisaddons.module.security.dom.feature.ApplicationFeatureId#getType() feature type} of the
     * feature.
     *
     * <p>
     *     The combination of the feature type and the {@link #getFeatureFqn() fully qualified name} is used to build
     *     the corresponding {@link #getFeature() feature} (view model).
     * </p>
     *
     * @see #getFeatureFqn()
     */
    @Programmatic
    public ApplicationFeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(final ApplicationFeatureType featureType) {
        this.featureType = featureType;
    }
    //endregion

    //region > featureFqn

    @javax.jdo.annotations.Column(allowsNull="false")
    private String featureFqn;

    /**
     * The {@link org.isisaddons.module.security.dom.feature.ApplicationFeatureId#getFullyQualifiedName() fully qualified name}
     * of the feature.
     *
     * <p>
     *     The combination of the {@link #getFeatureType() feature type} and the fully qualified name is used to build
     *     the corresponding {@link #getFeature() feature} (view model).
     * </p>
     *
     * @see #getFeatureType()
     */
    @Programmatic
    public String getFeatureFqn() {
        return featureFqn;
    }

    public void setFeatureFqn(final String featureFqn) {
        this.featureFqn = featureFqn;
    }

    //endregion

    //region > delete (action)
    public static class DeleteDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DeleteDomainEvent.class
    )
    @MemberOrder(sequence = "1")
    public ApplicationRole delete(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Are you sure?")
            final Boolean areYouSure) {
        final ApplicationRole owningRole = getRole();
        container.removeIfNotAlready(this);
        return owningRole;
    }
    public String validateDelete(final Boolean areYouSure) {
        return not(areYouSure) ? "Please confirm this action": null;
    }

    static boolean not(final Boolean areYouSure) {
        return areYouSure == null || !areYouSure;
    }
    //endregion
    
    //region > equals, hashCode, compareTo, toString
    private final static String propertyNames = "role, featureType, featureFqn, mode";

    @Override
    public int compareTo(final ApplicationPermission other) {
        return ObjectContracts.compare(this, other, propertyNames);
    }

    @Override
    public boolean equals(final Object obj) {
        return ObjectContracts.equals(this, obj, propertyNames);
    }

    @Override
    public int hashCode() {
        return ObjectContracts.hashCode(this, propertyNames);
    }

    @Override
    public String toString() {
        return ObjectContracts.toString(this, propertyNames);
    }

    public static class DefaultComparator implements Comparator<ApplicationPermission> {
        @Override
        public int compare(final ApplicationPermission o1, final ApplicationPermission o2) {
            return Ordering.natural().compare(o1, o2);
        }
    }
    //endregion

    //region > Functions

    public static final class Functions {

        private Functions(){}

        public static final Function<ApplicationPermission, ApplicationPermissionValue> AS_VALUE = new Function<ApplicationPermission, ApplicationPermissionValue>() {
            @Override
            public ApplicationPermissionValue apply(final ApplicationPermission input) {
                return new ApplicationPermissionValue(input.getFeatureId(), input.getRule(), input.getMode());
            }
        };


        public static final Function<ApplicationPermission, String> GET_FQN = new Function<ApplicationPermission, String>() {
            @Override
            public String apply(final ApplicationPermission input) {
                return input.getFeatureFqn();
            }
        };

    }
    //endregion

    //region  > services (injected)
    @javax.inject.Inject
    DomainObjectContainer container;

    @javax.inject.Inject
    ApplicationFeatureRepository applicationFeatureRepository;

    @javax.inject.Inject
    ApplicationRoleRepository applicationRoleRepository;

    //endregion

}
