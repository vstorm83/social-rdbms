/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.mysql.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jcr.Session;
import javax.persistence.EntityManager;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.addons.storage.dao.jpa.GenericDAOImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.jboss.byteman.contrib.bmunit.BMUnit;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml")
})
public abstract class BaseCoreTest extends BaseExoTestCase {
  private final Log LOG = ExoLogger.getLogger(BaseCoreTest.class);
  protected SpaceService spaceService;
  protected IdentityManager identityManager;
  protected RelationshipManager relationshipManager;
  protected ActivityManager activityManager;
  protected ActivityStorage activityStorage;

  protected Identity rootIdentity;
  protected Identity johnIdentity;
  protected Identity maryIdentity;
  protected Identity demoIdentity;

  protected Session session;
  
  public static boolean wantCount = false;
  private static int count;
  private int maxQuery;
  private boolean hasByteMan;

  @Override
  protected void setUp() throws Exception {
    // If is query number test, init byteman
    hasByteMan = getClass().isAnnotationPresent(QueryNumberTest.class);
    if (hasByteMan) {
      count = 0;
      maxQuery = 0;
      BMUnit.loadScriptFile(getClass(), "queryCount", "src/test/resources");
    }
    
    identityManager = getService(IdentityManager.class);
    activityManager =  getService(ActivityManager.class);
    activityStorage = getService(ActivityStorage.class);
    relationshipManager = getService(RelationshipManager.class);
    spaceService = getService(SpaceService.class);
    //
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
  }

  @Override
  protected void tearDown() throws Exception {
  }  
  
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  
//Fork from Junit 3.8.2
 @Override
 /**
  * Override to run the test and assert its state.
  * @throws Throwable if any exception is thrown
  */
 protected void runTest() throws Throwable {
   String fName = getName();
   assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
   Method runMethod= null;
   try {
     // use getMethod to get all public inherited
     // methods. getDeclaredMethods returns all
     // methods of this class but excludes the
     // inherited ones.
     runMethod= getClass().getMethod(fName, (Class[])null);
   } catch (NoSuchMethodException e) {
     fail("Method \""+fName+"\" not found");
   }
   if (!Modifier.isPublic(runMethod.getModifiers())) {
     fail("Method \""+fName+"\" should be public");
   }

   try {
     MaxQueryNumber queryNumber = runMethod.getAnnotation(MaxQueryNumber.class);
     if (queryNumber != null) {
       wantCount = true;
       maxQuery = queryNumber.value();
     }
     runMethod.invoke(this);
   }
   catch (InvocationTargetException e) {
     e.fillInStackTrace();
     throw e.getTargetException();
   }
   catch (IllegalAccessException e) {
     e.fillInStackTrace();
     throw e;
   }
   
   if (hasByteMan) {
     if (wantCount && count > maxQuery) {
       throw new AssertionFailedError(""+ count + " JDBC queries was executed but the maximum is : " + maxQuery);
     }
   }
 }

 // Called by byteman
 public static void count() {
   ++count;
  }

  /**
   * Creates new space with out init apps.
   *
   * @param space
   * @param creator
   * @param invitedGroupId
   * @return
   * @since 1.2.0-GA
   */
  protected Space createSpaceNonInitApps(Space space, String creator, String invitedGroupId) {
    // Creates new space by creating new group
    String groupId = null;
    try {
      groupId = SpaceUtils.createGroup(space.getDisplayName(), creator);
    } catch (SpaceException e) {
      LOG.error("Error while creating group", e);
    }

    if (invitedGroupId != null) {
      // Invites user in group join to new created space.
      // Gets users in group and then invites user to join into space.
      OrganizationService org = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      try {
        ListAccess<User> groupMembersAccess = org.getUserHandler().findUsersByGroupId(invitedGroupId);
        List<User> users = Arrays.asList(groupMembersAccess.load(0, groupMembersAccess.getSize()));

        for (User user : users) {
          String userId = user.getUserName();
          if (!userId.equals(creator)) {
            String[] invitedUsers = space.getInvitedUsers();
            if (!ArrayUtils.contains(invitedUsers, userId)) {
              invitedUsers = (String[]) ArrayUtils.add(invitedUsers, userId);
              space.setInvitedUsers(invitedUsers);
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to invite users from group " + invitedGroupId, e);
      }
    }
    String[] managers = new String[] { creator };
    space.setManagers(managers);
    space.setGroupId(groupId);
    space.setUrl(space.getPrettyName());
    try {
      spaceService.createSpace(space, creator);
    } catch (Exception e) {
      LOG.warn("Error while saving space", e);
    }
    return space;
  }

  protected void loginUser(String userId) {
    MembershipEntry membershipEntry = new MembershipEntry("/platform/user", "*");
    Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
    membershipEntries.add(membershipEntry);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
  
  /** Concurrency area**/
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  
  /**
   * Void Callable
   * @author thanhvc
   *
   */
  protected static abstract class VoidCallable implements Callable<Void> {
    /** */
    public abstract void execute();
    
    @Override
    public Void call() throws Exception {
      execute();
      return null;
    }
    
  }
  
  /**
   * Executes in the session lifecycle
   * @author thanhvc
   *
   * @param <T>
   */
  protected static interface SessionCallable<T> {
    T execute();
  }
  
  /**
   * Executes the Session Void Callable
   * @author thanhvc
   *
   */
  protected static interface SessionVoidCallable {
    void execute();
  }
  
  protected static abstract class TransactionCallable<T> implements SessionCallable<T> {
    void beforeTransactionCompletion() {
    }

    void afterTransactionCompletion() {
    }
  }

  protected static abstract class TransactionVoidCallable implements SessionVoidCallable {
    protected void beforeTransactionCompletion() {
    }

    protected void afterTransactionCompletion() {

    }
  }
  
  protected <T> T doInTransaction(TransactionCallable<T> callable) {
    T result = null;
    try {
      callable.beforeTransactionCompletion();
      boolean begunEM = GenericDAOImpl.startSynchronization();
      try {
        boolean begunTx = GenericDAOImpl.beginTransaction();
        try {
          result = callable.execute();
        } finally {
          GenericDAOImpl.endTransaction(begunTx);
        }
      } finally {
        GenericDAOImpl.stopSynchronization(begunEM);
      }
    } catch (RuntimeException e) {
      throw e;
    } finally {
      callable.afterTransactionCompletion();
    }
    return result;
  }

  protected void doInTransaction(TransactionVoidCallable callable) {
    try {
      callable.beforeTransactionCompletion();
      boolean begun = GenericDAOImpl.beginTransaction();
      callable.execute();
      GenericDAOImpl.endTransaction(begun);
    } catch (RuntimeException e) {
      throw e;
    } finally {
      callable.afterTransactionCompletion();
    }
  }

  protected void executeSync(VoidCallable callable) {
    executeSync(Collections.singleton(callable));
  }

  protected void executeSync(Collection<VoidCallable> callables) {
    try {
      List<Future<Void>> futures = executorService.invokeAll(callables);
      for (Future<Void> future : futures) {
        future.get();
      }
    } catch (ExecutionException e) {
      System.out.println(e);
      throw new RuntimeException(e);
    } catch (InterruptedException iex) {
      System.out.println(iex);
      throw new RuntimeException(iex);
    }
  }

  protected <T> void executeAsync(Runnable callable, final Runnable completionCallback) {
    final Future future = executorService.submit(callable);
    new Thread() {
      public void run() {
        while (!future.isDone()) {
          try {
            Thread.sleep(100);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        }
        try {
          completionCallback.run();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      };

    }.start();
  }

  protected Future<?> executeAsync(Runnable callable) {
    return executorService.submit(callable);
  }

  protected void awaitOnLatch(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  protected void sleep(int millis) {
    sleep(millis, null);
  }

  protected <V> V sleep(int millis, Callable<V> callable) {
    V result = null;
    try {
      LOG.info("Wait {} ms!", millis);
      if (callable != null) {
        result = callable.call();
      }
      Thread.sleep(millis);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return result;
  }
  
  protected List<ExoSocialActivity> listOf(int n, Identity poster, boolean isComment, boolean isSave) {
    List<ExoSocialActivity> list = new LinkedList<ExoSocialActivity>();
    ExoSocialActivity a = null;
    for (int i = 0; i < n; i++) {
      a = ActivityBuilder.getInstance()
                         .posterId(poster.getId())
                         .title("title " + i)
                         .body("body " + i)
                         .titleId("titleId " + i)
                         .isComment(isComment)
                         .take();
      
      if (isSave) {
        activityStorage.saveActivity(poster, a);
      }
      list.add(a);
    }
    
    return list;
  }
  
  protected ExoSocialActivity oneOfActivity(String title, final Identity poster, final boolean isComment, boolean isSave) {
    final ExoSocialActivity a = ActivityBuilder.getInstance()
                                         .posterId(poster.getId())
                                         .title(title)
                                         .body("body ")
                                         .titleId("titleId ")
                                         .isComment(isComment)
                                         .take();
    
    

    if (isSave) {
      doInTransaction(new TransactionCallable<ExoSocialActivity>() {
        @Override
        public ExoSocialActivity execute() {
          return activityStorage.saveActivity(poster, a);
        }
      });
    }
    return a;
  }
  
  protected ExoSocialActivity oneOfComment(String title, final Identity commenter) {
    return ActivityBuilder.getInstance()
                                         .posterId(commenter.getId())
                                         .title(title)
                                         .body("body ")
                                         .titleId("titleId ")
                                         .isComment(true)
                                         .take();
  }

  protected List<ExoSocialActivity> listOf(int n, Identity streamOwner, Identity poster, boolean isComment, boolean isSave) {
    List<ExoSocialActivity> list = new LinkedList<ExoSocialActivity>();
    ExoSocialActivity a = null;
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    
    for (int i = 0; i < n; i++) {
      a = ActivityBuilder.getInstance()
                         .posterId(poster.getId())
                         .title("title" + i)
                         .body("body" + i)
                         .titleId("titleId" + i)
                         .params(templateParams)
                         .isComment(isComment)
                         .take();
      
      if (isSave) {
        activityStorage.saveActivity(streamOwner, a);
      }
      list.add(a);
    }
    
    return list;
  }
  
  /**
   * Activity Builder
   * @author thanhvc
   *
   */
  public static class ActivityBuilder {
    /** */
    final ExoSocialActivity activity;
    
    /** */
    public ActivityBuilder(ExoSocialActivity activity) {
      this.activity = activity;
    }
    
    public static ActivityBuilder getInstance() {
      return new ActivityBuilder(new ExoSocialActivityImpl());
    }
    
    public static ActivityBuilder getInstance(ExoSocialActivity activity) {
      return new ActivityBuilder(activity);
    }
    
    public ActivityBuilder body(String body) {
      this.activity.setBody(body);
      return this;
    }
    
    public ActivityBuilder title(String title) {
      this.activity.setTitle(title);
      return this;
    }
    
    public ActivityBuilder titleId(String titleId) {
      this.activity.setTitleId(titleId);
      return this;
    }
    
    public ActivityBuilder posterId(String posterId) {
      this.activity.setPosterId(posterId);
      return this;
    }
    
    public ActivityBuilder isComment(boolean isComment) {
      this.activity.isComment(isComment);
      return this;
    }
    
    public ActivityBuilder params(Map<String, String> params) {
      this.activity.setTemplateParams(params);
      return this;
    }
    
    public ActivityBuilder commenters(String...commenters) {
      this.activity.setCommentedIds(commenters);
      return this;
    }
    
    public ActivityBuilder likers(String...likers) {
      this.activity.setLikeIdentityIds(likers);
      return this;
    }
    
    public ActivityBuilder mentioners(String...mentioners) {
      this.activity.setMentionedIds(mentioners);
      return this;
    }
    
    public ExoSocialActivity take() {
      return this.activity;
    }
  }
  
  
}