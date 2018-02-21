package com.mokocharlie.service

import akka.actor.ActorSystem
import com.mokocharlie.infrastructure.repository.ContactRepository
import com.typesafe.scalalogging.StrictLogging

class ContactService(contactRepo: ContactRepository)(implicit val system: ActorSystem)
    extends MokoCharlieService
    with StrictLogging {}
