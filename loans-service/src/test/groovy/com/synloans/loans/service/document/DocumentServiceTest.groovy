package com.synloans.loans.service.document

import com.synloans.loans.model.entity.document.Document
import com.synloans.loans.repository.document.DocumentRepository
import com.synloans.loans.service.document.impl.DocumentServiceImpl
import spock.lang.Specification

class DocumentServiceTest extends Specification{
    private DocumentService documentService
    private DocumentRepository documentRepository

    def setup(){
        documentRepository = Mock(DocumentRepository)
        documentService = new DocumentServiceImpl(documentRepository)
    }

    def "Тест. Сохранение документа"(){
        given:
            def document = Stub(Document)
        when:
            def savedDocument = documentService.save(document)
        then:
            savedDocument == document
            1 * documentRepository.save(document) >> document
    }

    def "Тест. Получение всех документов"(){
        given:
            def docs = [Stub(Document), Stub(Document), Stub(Document)]
        when:
            def allDocs = documentService.getAll()
        then:
            allDocs == docs
            1 * documentRepository.findAll() >> docs
    }

}
