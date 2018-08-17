package com.mokocharlie.service

import com.mokocharlie.domain.MailType.{MultiPart, Plain, Rich}
import com.mokocharlie.domain.{Mail, MailConfig}
import org.apache.commons.mail.EmailException

class MailService(config: MailConfig) {

  def send(mail: Mail): Either[EmailException, String] = {
    import org.apache.commons.mail._

    val format =
      if (mail.attachment.isDefined) MultiPart
      else if (mail.richMessage.isDefined) Rich
      else Plain

    val commonsMail: Email = format match {
      case Plain ⇒ new SimpleEmail().setMsg(mail.message)
      case Rich ⇒ new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
      case MultiPart ⇒
        val attachment = new EmailAttachment()
        attachment.setPath(mail.attachment.get.getAbsolutePath)
        attachment.setDisposition(EmailAttachment.ATTACHMENT)
        attachment.setName(mail.attachment.get.getName)
        new MultiPartEmail().attach(attachment).setMsg(mail.message)
    }

    commonsMail.addTo(mail.to.format)
    mail.cc.map(_.format).foreach(commonsMail.addCc)
    mail.bcc.map(_.format).foreach(commonsMail.addBcc)

    commonsMail.setHostName(config.host)
    commonsMail.setAuthentication(config.user, config.password)
    commonsMail.setTLS(config.tls)
    commonsMail.setSmtpPort(config.port)

    try {
      commonsMail
        .setFrom(mail.from.email, mail.from.name)
        .setSubject(mail.subject)
        .send()
      Right("sent")
    } catch {
        case ex: EmailException ⇒ Left(ex)
    }

  }
}
