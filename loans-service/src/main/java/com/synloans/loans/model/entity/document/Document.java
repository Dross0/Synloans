package com.synloans.loans.model.entity.document;

import com.synloans.loans.model.entity.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Document {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "create_date")
    private Instant createDate;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "body")
    private byte[] body;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

}
