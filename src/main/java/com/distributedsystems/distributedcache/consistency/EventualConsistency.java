package com.distributedsystems.distributedcache.consistency;

import org.springframework.stereotype.Component;

/*
In this implementation of eventual consistency all reads are local and for write we do a total order broadcast and
wait for acknowledgement from half of the controller replicas.
 */
@Component
public class EventualConsistency extends ConsistencyImpl {

}
