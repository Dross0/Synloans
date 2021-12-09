package com.synloans.loans.service

import com.synloans.loans.model.entity.Document
import com.synloans.loans.repositories.DocumentRepository
import spock.lang.Specification

class DocumentServiceTest extends Specification{
    private DocumentService documentService
    private DocumentRepository documentRepository

    def setup(){
        documentRepository = Mock(DocumentRepository)
        documentService = new DocumentService(documentRepository)
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

    def "Тест. Получение документа по id"(){
        when:
            def doc = documentService.getById(id)
        then:
            doc == resultDoc.orElse(null)
            1 * documentRepository.findById(id) >> resultDoc
        where:
            id  || resultDoc
            2   || Optional.empty()
            10  || Optional.of(Stub(Document))
    }

}
