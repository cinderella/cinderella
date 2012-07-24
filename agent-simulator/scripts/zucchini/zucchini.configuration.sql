# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
update configuration set value='pod' where name='network.dns.basiczone.updates';

update configuration set value='false' where name='use.user.concentrated.pod.allocation';
update configuration set value='firstfit' where name='vm.allocation.algorithm';

update configuration set value='60' where name='expunge.delay';
update configuration set value='60' where name='expunge.interval';
update configuration set value='3' where name='expunge.workers';
update configuration set value='10' where name='workers';

update configuration set value='0' where name='capacity.check.period';
update configuration set value='-1' where name='host.stats.interval';
update configuration set value='-1' where name='vm.stats.interval';
update configuration set value='-1' where name='storage.stats.interval';
update configuration set value='-1' where name='router.stats.interval';
update configuration set value='5' where name like 'vm.op.wait.interval';

update configuration set value='10.10.10.10' where name='xen.public.network.device';
update configuration set value='zcloud.simulator' where name='guest.domain.suffix';
update configuration set value='ZIM' where name='instance.name';

update configuration set value='1000' where name='direct.agent.load.size';
update configuration set value='10000' where name='default.page.size';
update configuration set value='4' where name='linkLocalIp.nums';
update configuration set value='true' where name like '%local.storage%';
update configuration set value='false' where name like '%check.pod.cidr%';

update configuration set value='100' where name like '%network.security%pool%';
update configuration set value='120' where name like 'network.securitygroups.work.cleanup.interval';
