package org.isisaddons.module.security.dom;

import com.danhaywood.java.testsupport.coverage.PojoTester;
import org.isisaddons.module.security.dom.feature.ApplicationFeature;
import org.isisaddons.module.security.dom.feature.ApplicationFeatureId;
import org.isisaddons.module.security.dom.permission.JdoApplicationPermission;
import org.isisaddons.module.security.dom.role.JdoApplicationRole;
import org.isisaddons.module.security.dom.tenancy.JdoApplicationTenancy;
import org.isisaddons.module.security.dom.user.JdoApplicationUser;
import org.joda.time.LocalDate;

/**
* Created by Dan on 04/09/2014.
*/
public class FixtureDatumFactories {

    public FixtureDatumFactories(){}

    public static PojoTester.FixtureDatumFactory<LocalDate> dates() {
        return new PojoTester.FixtureDatumFactory<>(LocalDate.class, null, new LocalDate(2012, 7, 19), new LocalDate(2012, 7, 20), new LocalDate(2012, 8, 19), new LocalDate(2013, 7, 19));
    }

    public static PojoTester.FixtureDatumFactory<Boolean> booleans() {
        return new PojoTester.FixtureDatumFactory<>(Boolean.class, null, Boolean.FALSE, Boolean.TRUE);
    }

    public static PojoTester.FixtureDatumFactory<ApplicationFeatureId> featureIds() {
        return new PojoTester.FixtureDatumFactory<>(ApplicationFeatureId.class, ApplicationFeatureId.newPackage("com.mycompany"), ApplicationFeatureId.newClass("com.mycompany.Foo"), ApplicationFeatureId.newMember("com.mycompany.Foo", "bar"));
    }

    public static PojoTester.FixtureDatumFactory<JdoApplicationRole> roles() {
        return new PojoTester.FixtureDatumFactory<>(JdoApplicationRole.class, new JdoApplicationRole(), new JdoApplicationRole());
    }

    public static PojoTester.FixtureDatumFactory<JdoApplicationUser> users() {
        return new PojoTester.FixtureDatumFactory<>(JdoApplicationUser.class, new JdoApplicationUser(), new JdoApplicationUser());
    }

    public static PojoTester.FixtureDatumFactory<JdoApplicationPermission> permissions() {
        return new PojoTester.FixtureDatumFactory<>(JdoApplicationPermission.class, new JdoApplicationPermission(), new JdoApplicationPermission());
    }

    public static PojoTester.FixtureDatumFactory<JdoApplicationTenancy> tenancies() {
        return new PojoTester.FixtureDatumFactory<>(JdoApplicationTenancy.class, new JdoApplicationTenancy(), new JdoApplicationTenancy());
    }

    public static PojoTester.FixtureDatumFactory<ApplicationFeature> features() {
        return new PojoTester.FixtureDatumFactory<>(ApplicationFeature.class, new ApplicationFeature(), new ApplicationFeature());
    }

}
