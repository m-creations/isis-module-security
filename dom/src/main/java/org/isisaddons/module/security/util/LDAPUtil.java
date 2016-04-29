package org.isisaddons.module.security.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.isis.security.shiro.IsisLdapRealm;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.isisaddons.module.security.shiro.ShiroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPUtil {

	private static final Logger logger = LoggerFactory.getLogger(LDAPUtil.class);

	public static LdapContext getLDAPContext() throws NamingException {
		RealmSecurityManager securityManager = ShiroUtils.getSecurityManager();
		Collection<Realm> realms = securityManager.getRealms();
		for (Iterator<Realm> iterator = realms.iterator(); iterator.hasNext();) {
			Realm realm = (Realm) iterator.next();
			if (realm instanceof IsisLdapRealm) {
				IsisLdapRealm isisLdapRealm = (IsisLdapRealm) realm;
				LdapContextFactory contextFactory = isisLdapRealm.getContextFactory();
				LdapContext ldapContext = contextFactory.getSystemLdapContext();
				return ldapContext;
			}
		}
		return null;
	}

	public static void addDoaminToGroupInLDAP(String group, String domain, Map<String, String> properties) {
		LdapContext ldapContext = null;
		try {
			ldapContext = LDAPUtil.getLDAPContext();
			Attributes atbs = new BasicAttributes();
			atbs.put(new BasicAttribute("domain", domain));
			ldapContext.modifyAttributes("cn=" + group + "," + properties.get("isis.prime.ldap.groupSearchBase"),
					DirContext.ADD_ATTRIBUTE, atbs);
		} catch (NamingException e) {
			throw new RuntimeException(
					"Error while adding domain to group in LDAP, group=" + group + ", domain=" + domain, e);
		} finally {
			LDAPUtil.closeLdapContext(ldapContext);
		}
	}

	public static void addUserToGroupInLDAP(String user, String group, String udn, String gdn) throws NamingException {
		addUserToGroupInLDAP(user, group, udn, gdn, LDAPUtil.getLDAPContext());
	}

	public static void addUserToGroupInLDAP(String user, String group, String udn, String gdn,
			LdapContext ldapContext) {
		try {
			Attributes atbs = new BasicAttributes();
			atbs.put(new BasicAttribute("roleOccupant", "uid=" + user + "," + udn));
			ldapContext.modifyAttributes("cn=" + group + "," + gdn, DirContext.ADD_ATTRIBUTE, atbs);
		} catch (NameAlreadyBoundException e) {
			logger.info(user + " user is already exist in LDAP");
		} catch (NamingException e) {
			throw new RuntimeException("Error while adding user to group in LDAP, group=" + group + ", user=" + user,
					e);
		} finally {
			LDAPUtil.closeLdapContext(ldapContext);
		}
	}


	public static String normalizeStringForLDAP(String input) {
		return input.replaceAll("[\\s|\\,|\\=\\;]", "");
	}

	public static void closeLdapContext(LdapContext ldapContext) {
		if (ldapContext != null) {
			try {
				ldapContext.close();
			} catch (NamingException e) {
				if (logger.isErrorEnabled())
					logger.error("Closing ldapContext failed:" + e.getMessage());
			}
		}
	}
}
