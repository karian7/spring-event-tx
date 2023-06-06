package com.example.springeventtx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SpringEventTxApplication

fun main(args: Array<String>) {
	runApplication<SpringEventTxApplication>(*args)
}

@RestController
class EventController(
	val publishService: PublishService,
	private val eventRepository: EventRepository,
	private val childServiceRepository: ChildServiceRepository
) {
	@GetMapping("/")
	fun index() {
		publishService.publish()
	}

	@GetMapping("/show")
	fun show(): Map<String, MutableIterable<*>> {
		return mapOf(
			"eventRepo" to eventRepository.findAll(),
			"childRepo" to childServiceRepository.findAll()
		)
	}
}

@Service
class PublishService(private val applicationEventPublisher: ApplicationEventPublisher, private val eventRepository: EventRepository) {
	@Transactional
	fun publish() {
		eventRepository.save(MyEvent(null, "my event"))
		applicationEventPublisher.publishEvent(TestEvent("my event"))
	}
}

data class TestEvent(
	val id: String
)

@Table
class MyEvent(@Id var id: Long?, val description: String)

interface EventRepository : CrudRepository<MyEvent, Long>

@Service
class Listener(
	private val childService: ChildService
) {
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	fun listen(testEvent: TestEvent) {
		childService.save(testEvent.id)
	}
}

@Service
class ChildService(
	private val repository: ChildServiceRepository
) {
	@Transactional
	fun save(id: String) {
		repository.save(Child(null, id))
	}
}

@Table
class Child(@Id var id: Long?, val eventId: String)

interface ChildServiceRepository : CrudRepository<Child, Long>
