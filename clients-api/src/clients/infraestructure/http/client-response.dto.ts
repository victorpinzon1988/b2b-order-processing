import { ClientSegment } from "src/clients/domain/client-segment.enum";
import { TaxRegime } from "src/clients/domain/tax-regime.enum";

export class ClientResponseDto{
    clientId!: string;
    name!: string;
    segment!: ClientSegment;
    taxRegime!: TaxRegime;
    region!: string;
}