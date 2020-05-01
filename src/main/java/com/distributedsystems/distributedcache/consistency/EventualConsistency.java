package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import org.springframework.stereotype.Component;

/*
In this implementation of eventual consistency all reads and writes are local
 */
@Component
public class EventualConsistency extends ConsistencyImpl {

}
