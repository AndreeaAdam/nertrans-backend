package ro.nertrans.filters.filteredServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import ro.nertrans.filters.filteredDTOS.PaymentDocumentSearchDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.repositories.PaymentDocumentRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class PaymentDocSearchService {
    @Inject
    private MongoTemplate mongoTemplate;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;

    public Page<PaymentDocument> listPaymentDocsFiltered(PaymentDocumentSearchDTO paymentDocumentSearchDTO, int page, int size, String sort, String dir, HttpServletRequest request) {
        Pageable pageable;
        if (size == -1 && paymentDocumentRepository.findAll().size() > 0) {
            pageable = PageRequest.of(page, paymentDocumentRepository.findAll().size());
        } else {
            pageable = PageRequest.of(page, size);
        }
        Query dynamicQuery;
        /**
         * @Improvable
         */
        if (dir != null && sort != null && (dir.equals("asc") || dir.equals("desc"))) {
            if (dir.equals("asc")) {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.ASC, sort));
            } else {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.DESC, sort));
            }
        } else {
            dynamicQuery = new Query().with(pageable);
        }
        if (paymentDocumentSearchDTO.getName() != null) {
            Criteria paymentDocNameCriteria = Criteria.where("name").regex(paymentDocumentSearchDTO.getName(), "i");
            dynamicQuery.addCriteria(paymentDocNameCriteria);
        }
        if (paymentDocumentSearchDTO.getCurrency() != null) {
            Criteria paymentCurrencyCriteria = Criteria.where("currency").is(paymentDocumentSearchDTO.getCurrency());
            dynamicQuery.addCriteria(paymentCurrencyCriteria);
        }
        if (paymentDocumentSearchDTO.getUserId() != null) {
            Criteria paymentDocUserIdCriteria = Criteria.where("userId").is(paymentDocumentSearchDTO.getUserId());
            dynamicQuery.addCriteria(paymentDocUserIdCriteria);
        }
        if (paymentDocumentSearchDTO.getPaymentMethod() != null) {
            Criteria paymentMethodCriteria = Criteria.where("paymentMethod").is(paymentDocumentSearchDTO.getPaymentMethod());
            dynamicQuery.addCriteria(paymentMethodCriteria);
        }
        if (paymentDocumentSearchDTO.getPartnerId() != null) {
            Criteria partnerIdCriteria = Criteria.where("partnerId").is(paymentDocumentSearchDTO.getPartnerId());
            dynamicQuery.addCriteria(partnerIdCriteria);
        }
        if (paymentDocumentSearchDTO.getStatus() != null) {
            Criteria statusCriteria = Criteria.where("status").is(paymentDocumentSearchDTO.getStatus());
            dynamicQuery.addCriteria(statusCriteria);
        }
        if (paymentDocumentSearchDTO.getDocSeries() != null) {
            Criteria docSeriesCriteria = Criteria.where("docSeries").is(paymentDocumentSearchDTO.getDocSeries());
            dynamicQuery.addCriteria(docSeriesCriteria);
        }
        dynamicQuery.collation(Collation.of("en").
                strength(Collation.ComparisonLevel.secondary()));
        List<PaymentDocument> list = mongoTemplate.find(dynamicQuery, PaymentDocument.class);

        return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoTemplate.count(Query.of(dynamicQuery).limit(-1).skip(-1), PaymentDocument.class));
    }
}
