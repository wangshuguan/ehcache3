/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.jsr107;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.cache.Cache;
import javax.cache.CacheManager;

import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static java.util.Arrays.asList;
import static org.ehcache.config.units.MemoryUnit.MB;
import static org.ehcache.config.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import org.ehcache.config.ResourcePoolsBuilder;
import static org.ehcache.config.ResourcePoolsBuilder.newResourcePoolsBuilder;
import static org.ehcache.config.units.EntryUnit.ENTRIES;
import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ResourceCombinationsTest {

  @Parameters
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
      { newResourcePoolsBuilder().heap(100, ENTRIES) },
      { newResourcePoolsBuilder().heap(100, ENTRIES).offheap(5, MB) },
      { newResourcePoolsBuilder().heap(100, ENTRIES).disk(1000, ENTRIES) },
    });
  }

  private final ResourcePools resources;

  @Rule
  public final TemporaryFolder diskPath = new TemporaryFolder();

  public ResourceCombinationsTest(ResourcePoolsBuilder resources) {
    this.resources = resources.build();
  }

  @Test
  public void testBasicCacheOperation() throws IOException, URISyntaxException {
    URI config = ResourceCombinationsTest.class.getResource("/ehcache-107-disk.xml").toURI();
    CacheManager cacheManager = new EhcacheCachingProvider().getCacheManager(config, ResourceCombinationsTest.class.getClassLoader());
    try {
      Cache<String, String> cache = cacheManager.createCache("test", fromEhcacheCacheConfiguration(
                      newCacheConfigurationBuilder().withResourcePools(resources).buildConfig(String.class, String.class)));
      cache.put("foo", "bar");
      assertThat(cache.get("foo"), is("bar"));
    } finally {
      cacheManager.close();
    }
  }
}
