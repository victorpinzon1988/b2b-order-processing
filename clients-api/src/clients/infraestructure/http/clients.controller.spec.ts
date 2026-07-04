import { TestingModule, Test } from "@nestjs/testing";
import { ClientsController } from "./clients.controller"
import { GetClientService } from "../../application/services/get-client.service";
import { ClientSegment } from "../../domain/client-segment.enum";
import { TaxRegime } from "../../domain/tax-regime.enum";

describe('ClientsController', () => {
    let controller: ClientsController;

    const getClientService = {
        execute: jest.fn(),
    };

    beforeEach(async () => {
        jest.clearAllMocks();

        const module: TestingModule = await Test.createTestingModule({
            controllers: [ClientsController],
            providers: [
                {
                    provide: GetClientService,
                    useValue: getClientService
                },
            ],
        }).compile();

        controller = module.get<ClientsController>(ClientsController);

    });


    it('should return client by id', async() => {
        getClientService.execute.mockResolvedValue({
            clientId: 'CLI-99821',
            name: 'Distribuidora Andina S.A.S',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Valle del Cauca',
        });

        const result = await controller.getClient('CLI-99821');

        expect(result).toEqual({
            clientId: 'CLI-99821',
            name: 'Distribuidora Andina S.A.S',
            segment: ClientSegment.MAYORISTA,
            taxRegime: TaxRegime.RESPONSABLE_IVA,
            region: 'Valle del Cauca',
        });

        expect(getClientService.execute).toHaveBeenCalledWith('CLI-99821');
        expect(getClientService.execute).toHaveBeenCalledTimes(1)
    });

})