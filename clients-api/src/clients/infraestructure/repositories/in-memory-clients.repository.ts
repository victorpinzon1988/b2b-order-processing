import { Injectable } from "@nestjs/common";
import { ClientsRepository } from "../../application/ports/clients.repository";
import { ClientSegment } from "../../domain/client-segment.enum";
import { Client } from "../../domain/client.entity";
import { TaxRegime } from "../../domain/tax-regime.enum";

@Injectable()
export class InMemoryClientsRepository implements ClientsRepository{
    private readonly clients: Client[] = [
        {
            clientId: 'CLI-99821',
            name: 'Distribuidora Andina S.A.S',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Valle del Cauca',
            },
            {
            clientId: 'CLI-10001',
            name: 'Comercializadora Norte Ltda',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Antioquia',
            },
            {
            clientId: 'CLI-10002',
            name: 'Minimercado La Esquina',
            segment: ClientSegment.MINORISTA,
            taxRegime: TaxRegime.NO_RESPONSABLE,
            region: 'Cundinamarca',
            },
            {
            clientId: 'CLI-10003',
            name: 'Abarrotes del Pacífico',
            segment: ClientSegment.MINORISTA,
            taxRegime: TaxRegime.NO_RESPONSABLE,
            region: 'Nariño',
            },
            {
            clientId: 'CLI-10004',
            name: 'Mayorista Caribe S.A.',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Atlántico',
        },
    ];

    async findById(clientId: string): Promise<Client | null> {
        return this.clients.find((client) => client.clientId === clientId) ?? null;
    }
}