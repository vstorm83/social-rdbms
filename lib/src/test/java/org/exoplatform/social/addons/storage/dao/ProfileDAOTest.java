/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.addons.storage.dao;

import org.exoplatform.social.addons.storage.entity.IdentityEntity;
import org.exoplatform.social.addons.storage.entity.ProfileEntity;
import org.exoplatform.social.addons.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.addons.test.BaseCoreTest;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProfileDAOTest extends BaseCoreTest {
  private IdentityDAO identityDAO;

  private List<IdentityEntity> deleteIdentities = new ArrayList<>();

  private IdentityEntity identity;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityDAO = getService(IdentityDAO.class);

    identity = identityDAO.create(createIdentity());
  }

  @Override
  public void tearDown() throws Exception {
    for (IdentityEntity identity : deleteIdentities) {
      identityDAO.delete(identity);
    }

    identityDAO.delete(identity);

    super.tearDown();
  }

  public void testCreateProfile() {
    ProfileEntity profile = createProfile();
    identityDAO.update(identity);

    profile = identityDAO.findByIdentityId(profile.getIdentity().getId());
    assertNotNull(profile);
    assertEquals(1, profile.getExperiences().size());
    assertEquals(2, profile.getAvatarImage().length);
    assertEquals(0x01, profile.getAvatarImage()[0]);
  }

  public void testUpdateProfile() {
    ProfileEntity profile = createProfile();
    identityDAO.update(identity);

    profile = identityDAO.findByIdentityId(profile.getIdentity().getId());
    assertNotNull(profile);
    assertEquals("/profile/root", profile.getUrl());

    profile.setUrl("/profile/root_updated");
    profile.setExperiences(new ArrayList<ProfileExperienceEntity>());

    identityDAO.update(identity);

    profile = identityDAO.findByIdentityId(profile.getIdentity().getId());

    assertNotNull(profile);
    assertEquals(0, profile.getExperiences().size());
    assertEquals("/profile/root_updated", profile.getUrl());
  }

  private ProfileEntity createProfile() {
    ProfileEntity profile = new ProfileEntity();
    profile.setIdentity(identity);
    profile.setCreatedDate(new Date());
    profile.setUrl("/profile/root");
    profile.setAvatarURL("/profile/root/avatar.png");

    profile.setAvatarImage(new byte[]{0x01, 0x02});

    ProfileExperienceEntity exp = new ProfileExperienceEntity();
    exp.setCompany("eXo Platform");
    exp.setPosition("Developer");
    exp.setSkills("Java, Unit test");
    exp.setStartDate("2015-01-01");
    List<ProfileExperienceEntity> exps = new ArrayList<>();
    exps.add(exp);
    profile.setExperiences(exps);

    return profile;
  }

  private IdentityEntity createIdentity() {
    IdentityEntity identity = new IdentityEntity();
    identity.setProviderId(OrganizationIdentityProvider.NAME);
    identity.setRemoteId("user_test_profile");
    identity.setEnabled(true);
    identity.setDeleted(false);

    return identity;
  }
}
