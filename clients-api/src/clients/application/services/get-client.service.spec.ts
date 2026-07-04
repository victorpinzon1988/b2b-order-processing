import { GetClientService } from "./get-client.service";
import { ClientsRepository } from "../ports/clients.repository";
import { ClientSegment } from "../../domain/client-segment.enum";
import { TaxRegime } from "../../domain/tax-regime.enum";
import { NotFoundException } from "@nestjs/common";

describe('GetClientService', () => {
    let service: GetClientService

    const repository: jest.Mocked<ClientsRepository> = {
        findById: jest.fn(),
    }

    beforeEach(() => {
        jest.clearAllMocks();
        service = new GetClientService(repository);
    });

    it('should return a client when it exists', async () => {
        repository.findById.mockResolvedValue({
            clientId: 'CLI-99821',
            name: 'Distribuidora Andina S.A.S',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Valle del Cauca',
        });

        const result = await service.execute('CLI-99821')

        expect(result).toEqual({
            clientId: 'CLI-99821',
            name: 'Distribuidora Andina S.A.S',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Valle del Cauca',
        });

        expect(repository.findById).toHaveBeenCalledWith('CLI-99821')
        expect(repository.findById).toHaveBeenCalledTimes(1)
    });


    it('should throw NotFoundException when client does not exist', async () => {
        repository.findById.mockResolvedValue(null);

        await expect(service.execute('CLI-404')).rejects.toBeInstanceOf(
            NotFoundException,
        );

        expect(repository.findById).toHaveBeenCalledWith('CLI-404');
        expect(repository.findById).toHaveBeenCalledTimes(1);
    });

});

