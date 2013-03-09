/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.broker.impl.caching;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.Notification;
import net.reliableresponse.notification.NotificationMessage;
import net.reliableresponse.notification.broker.BrokerFactory;
import net.reliableresponse.notification.broker.NotificationBroker;
import net.reliableresponse.notification.providers.NotificationProvider;
import net.reliableresponse.notification.usermgmt.Member;
import net.reliableresponse.notification.usermgmt.User;

/**
 * @author drig
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CachingNotificationBroker implements NotificationBroker {
	protected NotificationBroker realBroker;
	protected Cache notifications;
	
	public CachingNotificationBroker (NotificationBroker realBroker) {
		this.realBroker = realBroker;
		 notifications = new Cache(BrokerFactory.getConfigurationBroker().getIntValue("cache.maxobjects", 1200), 
					BrokerFactory.getConfigurationBroker().getIntValue("cache.maxseconds", 36000), 
					Cache.METHOD_FIFO);
	}
	
	public Cache getCache() {
		return notifications;
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#addNotification(net.reliableresponse.notification.Notification)
	 */
	public void addNotification(Notification notification) {
		notifications.addElement(notification);
		realBroker.addNotification(notification);	

	}

	public void addMessage(Notification notification, NotificationMessage message) {
		realBroker.addMessage(notification, message);
	}
	
	
	
	public NotificationMessage[] getNotificationMessages(
			Notification notification) {
		return realBroker.getNotificationMessages(notification);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#addProviderInformation(net.reliableresponse.notification.Notification, net.reliableresponse.notification.providers.NotificationProvider, java.util.Hashtable)
	 */
	public void addProviderInformation(Notification notification,
			NotificationProvider provider, Hashtable parameters, String status) {
		realBroker.addProviderInformation(notification, provider, parameters, status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationByUuid(java.lang.String)
	 */
	public Notification getNotificationByUuid(String uuid) {
		Notification notification = (Notification)notifications.getByUuid(uuid);
		if (notification == null) {
			notification = realBroker.getNotificationByUuid(uuid);
			if (notification != null) {
				notifications.addElement(notification);
			}
		}
		return notification;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getChildren(net.reliableresponse.notification.Notification)
	 */
	public Notification[] getChildren(Notification parent) {
		String[] childrenUuids = getChildrenUuids(parent);
		Notification[] children = new Notification[childrenUuids.length];
		for (int i = 0; i < children.length; i++) {
			children[i] = getNotificationByUuid(childrenUuids[i]);
		}
		return children;
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#setNotificationStatus(net.reliableresponse.notification.Notification, java.lang.String)
	 */
	public void setNotificationStatus(Notification notification, String status) {
		if (notifications.contains(notification)) {
			notifications.remove(notification);
		}
		notifications.addElement(notification);
		realBroker.setNotificationStatus(notification, status);
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSentTo(net.reliableresponse.notification.usermgmt.Member)
	 */
	public Notification[] getNotificationsSentTo(Member member) {
		String[] uuids = getUuidsSentTo(member);
		Notification[] notifications = new Notification[uuids.length];
		for (int i = 0; i < notifications.length; i++) {
			notifications[i] = getNotificationByUuid(uuids[i]);
		}
		return notifications;
	}
	
	public Notification[] getNotificationsSentBy(User user) {
		String[] uuids = getUuidsSentBy(user);
		Notification[] notifications = new Notification[uuids.length];
		for (int i = 0; i < notifications.length; i++) {
			notifications[i] = getNotificationByUuid(uuids[i]);
		}
		return notifications;	
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSince(java.util.Date)
	 */
	public Notification[] getNotificationsSince(Date since) {
		String[] uuids = getUuidsSince(since);
		Notification[] notifications = new Notification[uuids.length];
		for (int i = 0; i < notifications.length; i++) {
			notifications[i] = getNotificationByUuid(uuids[i]);
		}
		return notifications;	
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getNotificationsSince(long)
	 */
	public Notification[] getNotificationsSince(long since) {
		String[] uuids = getUuidsSince(since);
		Notification[] notifications = new Notification[uuids.length];
		for (int i = 0; i < notifications.length; i++) {
			notifications[i] = getNotificationByUuid(uuids[i]);
		}
		return notifications;	
	}
	
	public String getUltimateParentUuid(String child) {
		return realBroker.getUltimateParentUuid(child);
	}


	public int deleteNotificationsBefore(Date before) {
		String[] uuids = getUuidsBefore(before);
		int numDeleted = realBroker.deleteNotificationsBefore(before);
		for (int i =0; i < uuids.length; i++) {
			Notification toRemove = (Notification)notifications.getByUuid(uuids[i]);
			if (toRemove != null) {
				notifications.remove(toRemove);
			}
		}
		return numDeleted;
	}
	public Notification[] getNotificationsBefore(Date before) {
		String[] uuids = getUuidsBefore(before);
		Notification[] notifications = new Notification[uuids.length];
		for (int i = 0; i < notifications.length; i++) {
			notifications[i] = getNotificationByUuid(uuids[i]);
		}
		return notifications;	
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getAllUnconfirmedNotifications()
	 */
	public Notification[] getAllUnconfirmedNotifications() {
		return realBroker.getAllUnconfirmedNotifications();
	}

	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getAllPendingNotifications()
	 */
	public Notification[] getAllPendingNotifications() {
        String[] uuids = getAllPendingUuids();
        if (uuids == null) {
                return new Notification[0];
        }
        Vector notifications = new Vector();
        for (int i = 0; i < uuids.length; i++) {
                Notification notification = getNotificationByUuid(uuids[i]);
                if (notification != null) {
                        notifications.addElement(notification);
                }
        }
        return (Notification[])notifications.toArray(new Notification[0]);
	}

	public int getNumNotifications() {
		return realBroker.getNumNotifications();
	}
	public int getNumPendingNotifications() {
		return realBroker.getNumPendingNotifications();
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersUnconfirmedNotifications(net.reliableresponse.notification.usermgmt.Member)
	 */
	public Notification[] getMembersUnconfirmedNotifications(Member member) {
		String[] uuids = getMembersUnconfirmedUuids(member);
		if (uuids == null) {
			uuids = new String[0];
		}
		Vector notifications = new Vector(uuids.length);
		for (int i = 0; i < uuids.length; i++) {
			Notification notif = getNotificationByUuid(uuids[i]);
			if (notif != null) {
				notifications.addElement(notif);
			}
		}
		
		return (Notification[])notifications.toArray(new Notification[0]);
	}
	
	

	public Notification[] getUpdatedNotificationsTo(Member member, Date since) {
		return realBroker.getUpdatedNotificationsTo(member, since);
	}

	public String[] getUpdatedUuidsTo(Member member, Date since) {
		return realBroker.getUpdatedUuidsTo(member, since);
	}
	/* (non-Javadoc)
	 * @see net.reliableresponse.notification.broker.NotificationBroker#getMembersPendingNotifications()
	 */
	public Notification[] getMembersPendingNotifications() {
		return realBroker.getMembersPendingNotifications();
	}

	public String[] getAllPendingUuids() {
		return realBroker.getAllPendingUuids();
	}
	public String[] getAllUnconfirmedUuids() {
		return realBroker.getAllUnconfirmedUuids();
	}
	public String[] getChildrenUuids(Notification parent) {
		return realBroker.getChildrenUuids(parent);
	}
	
	public String[] getMembersPendingUuids() {
		return realBroker.getMembersPendingUuids();
	}
	
	public String[] getMembersUnconfirmedUuids(Member member) {
		return realBroker.getMembersUnconfirmedUuids(member);
	}
	
	public String[] getUuidsSentTo(Member member) {
		return realBroker.getUuidsSentTo(member);
	}

	public String[] getUuidsSentBy(User user) {
		return realBroker.getUuidsSentBy(user);
	}

	public String[] getUuidsSince(Date since) {
		return realBroker.getUuidsSince(since);
	}
	public String[] getUuidsSince(long since) {
		return realBroker.getUuidsSince(since);
	}
	public String[] getUuidsBefore(Date before) {
		return realBroker.getUuidsBefore(before);
	}
	public void setOwner(Notification notification, String owner) {
		realBroker.setOwner(notification, owner);
	}	

	public String getEscalationStatus(Notification notification) {
		return realBroker.getEscalationStatus(notification);
	}

	public void logConfirmation(Member confirmedBy, Notification notification) {
		realBroker.logConfirmation(confirmedBy, notification);

	}
	
	public void logEscalation(Member from, Member to, Notification notification) {
		realBroker.logEscalation(from, to, notification);
	}
	
	public void logExpired(Notification notification) {
		realBroker.logExpired(notification);
	}
	
	public void logPassed(Member from, Member to, Notification notification) {
		realBroker.logPassed(from, to, notification);
	}
	
	public int countPastNotifs(Member member, long pastMillis) {
		return realBroker.countPastNotifs(member, pastMillis);
	}

	public java.util.Date getEarliestNotificationDate() {
		return realBroker.getEarliestNotificationDate();
	}
}